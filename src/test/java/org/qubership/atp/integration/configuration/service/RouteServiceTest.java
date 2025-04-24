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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.integration.configuration.component.RouteRegisterComponent;
import org.qubership.atp.integration.configuration.model.AtpRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class RouteServiceTest {

    @Autowired
    RouteService routeService;

    @MockBean
    RouteRegisterComponent routeRegisterComponent;

    @Test
    public void getRouteShouldReturnRouteInfo() {
        AtpRoute route = routeService.getRoute();
        Assert.assertEquals("mock-service", route.getServiceId());
        Assert.assertEquals("/api/mock-service/v1/**", route.getPath());
        Assert.assertTrue(route.isPublic());
    }
}