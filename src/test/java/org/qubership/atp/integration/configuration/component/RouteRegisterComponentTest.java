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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.integration.configuration.feign.PublicGatewayFeignClient;
import org.qubership.atp.integration.configuration.model.AtpRoute;
import org.qubership.atp.integration.configuration.service.RouteService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.FeignException;
import feign.Request;
import feign.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RouteService.class, RouteRegisterComponent.class})
public class RouteRegisterComponentTest {

    @MockBean
    PublicGatewayFeignClient publicGatewayFeignClient;
    @SpyBean
    RouteService routeService;
    @Autowired
    RouteRegisterComponent routeRegisterComponent;
    AtpRoute atpRoute;

    /**
     * Before test handler.
     */
    @Before
    public void before() {
        atpRoute = routeService.getRoute();
        doReturn(atpRoute).when(routeService).getRoute();
    }

    /**
     * Test of Public Gateway registering: negative scenario.
     */
    @Test
    public void routeRegisterPublicGatewayWhenRegisterFailedThenErrorLog() {
        atpRoute.setUrl("http://localhost");
        atpRoute.setPublic(true);
        doThrow(FeignException.errorStatus("POST", Response.builder()
                .request(Request.create(Request.HttpMethod.POST, atpRoute.getUrl(), new HashMap<>(),
                        atpRoute.toString().getBytes(), null))
                .status(500)
                .build()))
                .when(publicGatewayFeignClient).register(eq(atpRoute));
        List<ILoggingEvent> logsList = configureAndStartAppender();
        assertEquals("Cannot register route in public gateway 'http://atp-public-gateway:8080' "
                + "using feign client: [500] during [POST] to [http://localhost] [POST]: []",
                logsList.get(0).getFormattedMessage());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
    }

    /**
     * Test of Internal Gateway registering: negative scenario.
     */
    @Test
    public void routeRegisterInternalGatewayWhenRegisterFailedThenErrorLog() {
        List<ILoggingEvent> logsList = configureAndStartAppender();
        assertEquals("Cannot register route in http://atp-internal-gateway:8080/register",
                logsList.get(0).getFormattedMessage());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
    }

    private List<ILoggingEvent> configureAndStartAppender() {
        Logger fooLogger = (Logger) LoggerFactory.getLogger(RouteRegisterComponent.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        fooLogger.addAppender(listAppender);
        routeRegisterComponent.routeRegister();
        return listAppender.list;
    }
}