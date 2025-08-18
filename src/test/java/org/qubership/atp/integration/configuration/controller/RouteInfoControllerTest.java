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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.qubership.atp.integration.configuration.component.RouteRegisterComponent;
import org.qubership.atp.integration.configuration.service.AuditLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RouteInfoControllerTest {

    /**
     * MockMvc object.
     */
    private MockMvc mvc;

    /**
     * WebApplicationContext bean.
     */
    @Autowired
    WebApplicationContext webApplicationContext;

    /**
     * RouteRegisterComponent mockBean.
     */
    @MockBean
    RouteRegisterComponent routeRegisterComponent;

    /**
     * AuditLoggingService mockBean.
     */
    @MockBean
    AuditLoggingService auditLoggingService;

    /**
     * Setup webApplicationContext before test.
     */
    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Test of RouteInfo getting.
     *
     * @throws Exception in case errors faced.
     */
    @Test
    public void getRouteInfoTest() throws Exception {
        String uri = "/atp-integration/routeInfo";

        Mockito.doNothing().when(auditLoggingService)
                .loggingRequest(isA(HttpServletRequest.class), isA(HttpServletResponse.class));

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
    }
}
