/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.integration.configuration.component;

import org.qubership.atp.integration.configuration.feign.PublicGatewayFeignClient;
import org.qubership.atp.integration.configuration.model.AtpRoute;
import org.qubership.atp.integration.configuration.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

public class RouteRegisterComponent {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteRegisterComponent.class);

    /**
     * Route Service.
     */
    private final RouteService routeService;

    /**
     * Url to public gateway.
     */
    @Value("${atp.public.gateway.url:#{'http://atp-public-gateway:8080'}}")
    private String publicGatewayUrl;

    /**
     * Url to internal gateway.
     */
    @Value("${atp.internal.gateway.url:#{'http://atp-internal-gateway:8080'}}")
    private String internalGatewayUrl;

    /**
     * Public gateway feign client.
     */
    @Autowired
    private PublicGatewayFeignClient publicGatewayFeignClient;

    /**
     * Constructor.
     *
     * @param routeService Route Service.
     */
    public RouteRegisterComponent(final RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * RouteRegister Job.
     */
    @Scheduled(initialDelayString = "60000", fixedRateString = "60000")
    public void routeRegister() {
        register();
    }

    /**
     * Startup application listener.
     * Performs route registration.
     *
     * @param event ApplicationReadyEvent listened by this Event Listener.
     */
    @EventListener
    public void routeRegister(final ApplicationReadyEvent event) {
        register();
    }

    private void register() {
        AtpRoute atpRoute = routeService.getRoute();
        if (atpRoute.isPublic()) {
            try {
                publicGatewayFeignClient.register(atpRoute);
            } catch (Exception e) {
                LOGGER.error("Cannot register route in public gateway '{}' using feign client: {}",
                        publicGatewayUrl, e.getMessage());
            }
        }
        if (atpRoute.isInternal()) {
            register(this.internalGatewayUrl + "/register", atpRoute);
        }
    }

    private void register(final String gatewayUrl, final AtpRoute atpRoute) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AtpRoute> entity = new HttpEntity<>(atpRoute, headers);
        try {
            template.postForEntity(gatewayUrl, entity, AtpRoute.class);
            LOGGER.info("Route {} for {} was registered in {}",
                    atpRoute.getPath(), atpRoute.getServiceId(), gatewayUrl);
        } catch (Exception e) {
            LOGGER.error("Cannot register route in {}", gatewayUrl, e);
        }
    }
}
