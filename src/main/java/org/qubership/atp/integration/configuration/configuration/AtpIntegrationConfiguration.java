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

package org.qubership.atp.integration.configuration.configuration;

import org.qubership.atp.integration.configuration.component.RouteRegisterComponent;
import org.qubership.atp.integration.configuration.controller.RouteInfoController;
import org.qubership.atp.integration.configuration.service.RouteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(name = "eureka.client.enabled")
@EnableScheduling
public class AtpIntegrationConfiguration {

    /**
     * Get Route Service.
     *
     * @return new RouteService object.
     */
    @Bean
    public RouteService getRouteService() {
        return new RouteService();
    }

    /**
     * Get RouteRegisterComponent for routeService provided.
     *
     * @param routeService RouteService object
     * @return new RouteRegisterComponent for routeService parameter.
     */
    @Bean
    public RouteRegisterComponent getRouteRegisterComponent(final RouteService routeService) {
        return new RouteRegisterComponent(routeService);
    }

    /**
     * Get RouteInfoController for routeService provided.
     *
     * @param routeService RouteService object
     * @return new RouteInfoController for routeService parameter.
     */
    @Bean
    public RouteInfoController routeInfoController(final RouteService routeService) {
        return new RouteInfoController(routeService);
    }
}
