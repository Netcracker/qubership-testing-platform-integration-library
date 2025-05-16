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

import static org.qubership.atp.integration.configuration.service.AuditLoggingService.PROJECT_ID_HEADER_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpHeaders;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.qubership.atp.integration.configuration.configuration.AuditLoggingConfiguration;
import org.qubership.atp.integration.configuration.configuration.LoggingHelpersConfiguration;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.helpers.KafkaAdminHelper;
import org.qubership.atp.integration.configuration.protos.KafkaAuditLoggingMessage.AuditLoggingMessage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringBootTest(classes = {AuditLoggingConfiguration.class, LoggingHelpersConfiguration.class})
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "atp.audit.logging.enable=true",
        "spring.application.name=" + AuditLoggingServiceTest.TEST_SERVICE,
        "atp.audit.logging.topic.name=audit_logging_topic",
        "spring.kafka.producer.bootstrap-servers=localhost:9092"
})
public class AuditLoggingServiceTest {

    /**
     * AuditLoggingService bean.
     */
    @Autowired
    private AuditLoggingService auditLoggingService;

    /**
     * JwtParseHelper bean.
     */
    @Autowired
    private JwtParseHelper jwtParseHelper;

    /**
     * HttpRequestParseHelper bean.
     */
    @Autowired
    private HttpRequestParseHelper httpRequestParseHelper;

    /**
     * KafkaProducer bean.
     */
    @MockBean
    private KafkaProducer<UUID, AuditLoggingMessage> kafkaProducer;

    /**
     * KafkaAdminHelper bean.
     */
    @MockBean
    private KafkaAdminHelper kafkaAdminHelper;

    /**
     * ArgumentCaptor bean.
     */
    @Captor
    private ArgumentCaptor<ProducerRecord<UUID, AuditLoggingMessage>> recordCaptor;

    /**
     * Name of service.
     */
    public static final String TEST_SERVICE = "RAM";

    /**
     * UUID of session.
     */
    private static final String TEST_SESSION_ID = "8085b7d3-9472-470a-b914-d70071d2b072";

    /**
     * URL of some project resource.
     */
    private static final String TEST_URL = "/catalog/api/v1/projects/ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f/testplans";

    /**
     * UUID of some project.
     */
    private static final String TEST_PROJECT_ID = "ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f";

    /**
     * Some username.
     */
    private static final String TEST_USERNAME = "Admin Adminovich";

    /**
     * UUID of the user.
     */
    private static final String TEST_USER_ID = "c2344d70-3707-4418-a9c9-dbdb8beca796";

    /**
     * Valid Bearer Token.
     */
    private static final String TEST_AUTH_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVzOD"
            + "NZai05clhfZUItYWZuUWdPRzhDZUFhblFTakJZTS0wNjQxdVdVIn0.eyJqdGkiOiI1OTRmMzY0My1hODhmLTQ3MTQtYTM2NS1jYWJiZj"
            + "Y1MmY5MWYiLCJleHAiOjE2NjIzNzk3NzIsIm5iZiI6MCwiaWF0IjoxNjYyMzc2MTcyLCJpc3MiOiJodHRwczovL2F0cC1rZXljbG9hay"
            + "1kZXYyMjIuZGV2LWF0cC1jbG91ZC5uZXRjcmFja2VyLmNvbS9hdXRoL3JlYWxtcy9hdHAyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6Im"
            + "MyMzQ0ZDcwLTM3MDctNDQxOC1hOWM5LWRiZGI4YmVjYTc5NiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImZyb250ZW5kIiwiYXV0aF90aW"
            + "1lIjowLCJzZXNzaW9uX3N0YXRlIjoiODA4NWI3ZDMtOTQ3Mi00NzBhLWI5MTQtZDcwMDcxZDJiMDcyIiwiYWNyIjoiMSIsImFsbG93ZW"
            + "Qtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsIkFUUF9BRE1JTiIsInVtYV9hdX"
            + "Rob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS"
            + "1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbH"
            + "NlLCJuYW1lIjoiQWRtaW4gQWRtaW5vdmljaCIsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIiwiZ2l2ZW5fbmFtZSI6IkFkbWluIi"
            + "wiZmFtaWx5X25hbWUiOiJBZG1pbm92aWNoIiwiZW1haWwiOiJ0ZXN0Nzc3QHRlc3QifQ.FKCnW9ae-Hza28NJdUVJKud26nHzz7mw9P2"
            + "O7Pec6GKcbs0nPY6Aabb4CvFL70MbXyOtvK-ErihOAJd__DM_dT0nNbPNA4CIP9rod3ylSVjSAxfw1FFh1lSnNwoZjs4K3JOFqmwnJN2"
            + "0ROuCYMo3EcJpFzZA2dt2GQEpj29N4Qk6a-dx3IG6Jz0T0LqEN1bjsd3EeiRXqm83wsYi3nkmPx4Yz538Op4QfS2UxDUriUQNaU9vXyo"
            + "FRNawE09X6C56Y3gTqytG2JDUZhKYcoY87sbERRVtJFVLrJxiGiGvrest92ATNrEVz2tI13y4SbiuaQAx0BOT5T4Iz9gAm_FkKg";

    /**
     * Test of request logging.
     */
    @Test
    public void logRequestTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER);
        request.addHeader(PROJECT_ID_HEADER_NAME, TEST_PROJECT_ID);
        request.addHeader("refer", "http");
        request.addHeader("User-Agent", "some-browser-version");
        request.setRequestURI(TEST_URL);
        request.setMethod("GET");
        request.setRemoteAddr("some-address");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("userAction", "userAction");
        MDC.setContextMap(contextMap);

        auditLoggingService.loggingRequest(request, response);

        verify(kafkaProducer, times(1)).send(recordCaptor.capture());

        ProducerRecord<UUID, AuditLoggingMessage> record = recordCaptor.getValue();
        Assertions.assertNotNull(record);

        AuditLoggingMessage message = record.value();
        Assertions.assertNotNull(message);

        Assertions.assertEquals(TEST_SESSION_ID, message.getSessionId());
        Assertions.assertEquals(TEST_PROJECT_ID, message.getProjectId());
        Assertions.assertEquals(TEST_SERVICE, message.getService());
        Assertions.assertEquals(TEST_USERNAME, message.getUsername());
        Assertions.assertEquals(TEST_USER_ID, message.getUserId());
        Assertions.assertEquals(TEST_URL, message.getUrl());
        Assertions.assertTrue(new Date().after(new Date(message.getStartDate())));
    }
}
