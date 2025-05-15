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

package org.qubership.atp.integration.configuration.controller;

import org.qubership.atp.integration.configuration.model.AtpRoute;
import org.qubership.atp.integration.configuration.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("atp-integration")
public class RouteInfoController {
    private final RouteService routeService;

    /**
     * Constructor.
     *
     * @param routeService RouteService.
     */
    @Autowired
    public RouteInfoController(final RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Method serving "/routeInfo" endpoint. It returns getRoute() result of routeService.
     *
     * @return AtpRoute object - result of routeService.getRoute().
     */
    @GetMapping("/routeInfo")
    public AtpRoute getRouteInfo() {
        return routeService.getRoute();
    }
}
