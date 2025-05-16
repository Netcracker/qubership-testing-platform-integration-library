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

package org.qubership.atp.integration.configuration.service;

import org.qubership.atp.integration.configuration.model.AtpRoute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RouteService {

    /**
     * Spring application name configuration setting.
     */
    @Value("${spring.application.name}")
    private String serviceId;

    /**
     * Port number.
     */
    @Value("${server.port: #{8080}}")
    private int port;

    /**
     * Service path.
     */
    @Value("${atp.service.path:#{null}}")
    private String path;

    /**
     * Is the service public or not? (Should it be registered in public gateway or not?)
     */
    @Value("${atp.service.public: #{true}}")
    private boolean isPublic;

    /**
     * Is the service internal or not? (Should it be registered in internal gateway or not?)
     */
    @Value("${atp.service.internal: #{true}}")
    private boolean isInternal;

    /**
     * Context path.
     */
    @Value("#{servletContext.contextPath}")
    private String servletContextPath;

    /**
     * Service URL.
     */
    @Value("${atp.service.url:#{null}}")
    private String serviceUrl;

    /**
     * Prepares service route.
     *
     * @return new AtpRoute created and configured.
     */
    public AtpRoute getRoute() {
        AtpRoute atpRoute = new AtpRoute();
        atpRoute.setServiceId(serviceId);
        atpRoute.setPort(port);
        atpRoute.setPath(preparePath(path));
        atpRoute.setPublic(isPublic);
        atpRoute.setInternal(isInternal);
        atpRoute.setContextPath(servletContextPath);
        atpRoute.setUrl(serviceUrl);
        return atpRoute;
    }

    private String preparePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "/api/" + serviceId + "/v1/**";
        }
        return path;
    }
}
