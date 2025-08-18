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

package org.qubership.atp.integration.configuration.filters;

import static org.qubership.atp.integration.configuration.service.AuditLoggingService.PROJECT_ID_HEADER_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.integration.configuration.configuration.AuditLoggingConfiguration;
import org.qubership.atp.integration.configuration.configuration.LoggingHelpersConfiguration;
import org.qubership.atp.integration.configuration.helpers.KafkaAdminHelper;
import org.qubership.atp.integration.configuration.service.AuditLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {AuditLoggingConfiguration.class, LoggingHelpersConfiguration.class})
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "atp.audit.logging.enable=true",
        "atp.audit.logging.topic.name=audit_logging_topic",
        "atp.reporting.kafka.producer.bootstrap-server=localhost:9092"
})
public class AuditLoggingFilterTest {

    /**
     * AuditLoggingFilter Bean.
     */
    @Autowired
    private AuditLoggingFilter auditLoggingFilter;

    /**
     * KafkaAdminHelper MockBean.
     */
    @MockBean
    private KafkaAdminHelper kafkaAdminHelper;

    /**
     * AuditLoggingService MockBean.
     */
    @MockBean
    private AuditLoggingService auditLoggingService;

    /**
     * HttpServletRequest MockBean.
     */
    @MockBean
    private HttpServletRequest httpServletRequest;

    /**
     * HttpServletResponse MockBean.
     */
    @MockBean
    private HttpServletResponse httpServletResponse;

    /**
     * FilterChain MockBean.
     */
    @MockBean
    private FilterChain filterChain;

    /**
     * Valid Bearer Token for Authorisation Header value.
     */
    public static final String TEST_AUTH_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVzOD"
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
     * Valid Bearer Token for Authorisation Header value, especially for M2M.
     */
    public static final String TEST_M2M_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVzODN"
            + "Zai05clhfZUItYWZuUWdPRzhDZUFhblFTakJZTS0wNjQxdVdVIn0.eyJqdGkiOiI2ZTRhMzkyOC1lNmU4LTQxMWYtYWNmZC01NDI5YjM"
            + "0MzA2NzkiLCJleHAiOjE2NjI2MjU1NjMsIm5iZiI6MCwiaWF0IjoxNjYyNjIxOTYzLCJpc3MiOiJodHRwczovL2F0cC1rZXljbG9hay1"
            + "kZXYyMjIuZGV2LWF0cC1jbG91ZC5uZXRjcmFja2VyLmNvbS9hdXRoL3JlYWxtcy9hdHAyIiwiYXVkIjpbInJlYWxtLW1hbmFnZW1lbnQ"
            + "iLCJhY2NvdW50Il0sInN1YiI6IjJjYWJhZTM4LTQyMGEtNGMyMy04YzgzLTg4YjIxMGUzOTdjZCIsInR5cCI6IkJlYXJlciIsImF6cCI"
            + "6ImNhdGFsb2ciLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI2Mjg4YjNmOC0yZTAyLTQyYTEtODYxOS05MjBjYzU5NmI2ZjQ"
            + "iLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9odHRwOi8vIiwiaHR0cHM6Ly9yYW0taHR0cHMtZGV2MjIyLmRldi1"
            + "hdHAtY2xvdWQubmV0Y3JhY2tlci5jb20vKioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiQVRQX0F"
            + "ETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJyZWFsbS1tYW5hZ2VtZW50Ijp7InJvbGVzIjpbInZ"
            + "pZXctdXNlcnMiLCJxdWVyeS1ncm91cHMiLCJxdWVyeS11c2VycyJdfSwiY2F0YWxvZyI6eyJyb2xlcyI6WyJ1bWFfcHJvdGVjdGlvbiJ"
            + "dfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl1"
            + "9fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjbGllbnRIb3N0IjoiMTAuMjM2LjE1OC43OSI"
            + "sImNsaWVudElkIjoiY2F0YWxvZyIsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC1jYXRhbG9nIiwiY2xpZW50QWR"
            + "kcmVzcyI6IjEwLjIzNi4xNTguNzkiLCJlbWFpbCI6InNlcnZpY2UtYWNjb3VudC1jYXRhbG9nQHBsYWNlaG9sZGVyLm9yZyJ9.Q4xspJ"
            + "66Fm53g4OeH3p7ubIzUUr-UyqNdG1v5xoEo4PfPAWzTIWMcznPS7PfrxlHHLRuYZdk1htEEPuSYmyz0ILEa72NnycIO2lR4iEC_XZgRe"
            + "BnI8amqdQL3TNQrGGLz0IZTWhaEy3w45bkwNcjesGanK4BtxQyeGa9wmfHGRFibuukB2IoId2KhlnpxevEQLmbMhnbK4-pgN1dYpHMZM"
            + "gdmQKowUIK2fPEwPzEBnQSsxXmmsTXhUtFIz_szi8M2VT2XEZY5DiUXqmVaczZR6nPu6Zc1DCGtqv5NqtQ7cF9GCIhlODpxkDs655Q3J"
            + "X8rHkFrZfYFiVdoVQHZMqpaA";

    /**
     * Invalid Bearer Token for Authorisation Header value.
     */
    public static final String BROKEN_BEARER_HEADER = "Bearer eyJhbGciOiJSUz I1NiIsInR5cCIgOiAiS ldUIiwia2lkIiA6ICI5TWVzOD"
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
     * Basic Authorisation Header value.
     */
    public static final String TEST_BASIC_HEADER = "Basic 123";

    /**
     * URL to some of the project resources.
     */
    private static final String TEST_URL = "/catalog/api/v1/projects/ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f/testplans";

    /**
     * Project UUID.
     */
    private static final String TEST_PROJECT_ID = "ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f";

    /**
     * Before test handler.
     */
    @Before
    public void setUp() {
        when(httpServletRequest.getHeader(PROJECT_ID_HEADER_NAME)).thenReturn(TEST_PROJECT_ID);
        when(httpServletRequest.getRequestURI()).thenReturn(TEST_URL);
    }

    /**
     * Test audit logging in case Bearer Token is set.
     *
     * @throws Exception pass ServletException or IOException from inner function.
     */
    @Test
    public void userAuthHeaderSetExpectAuditLoggingCall() throws Exception {
        whenThenVerifyParametrizedInternal(TEST_AUTH_HEADER, 1, 1);
    }

    /**
     * Test audit logging skipping in case M2M Authorisation Header is set.
     *
     * @throws Exception pass ServletException or IOException from inner function.
     */
    @Test
    public void m2mAuthHeaderSetExpectAuditLoggingSkip() throws Exception {
        whenThenVerifyParametrizedInternal(TEST_M2M_HEADER, 1, 0);
    }

    /**
     * Test audit logging in case Authorisation Header is not set.
     *
     * @throws Exception pass ServletException or IOException from inner function.
     */
    @Test
    public void authHeaderNotSetExpectAuditLoggingSkip() throws Exception {
        whenThenVerifyParametrizedInternal(StringUtils.EMPTY, 1, 0);
    }

    /**
     * Test audit logging in case Basic Token is set.
     *
     * @throws Exception pass ServletException or IOException from inner function.
     */
    @Test
    public void authHeaderSetBasicTokenExpectAuditLoggingCall() throws Exception {
        whenThenVerifyParametrizedInternal(TEST_BASIC_HEADER, 1, 1);
    }

    /**
     * Test audit logging in case Bearer Token is set but ParseException is faced.
     *
     * @throws Exception pass ServletException or IOException from inner function.
     */
    @Test
    public void authHeaderSetBearerTokenParseExceptionExpectAuditLoggingCall() throws Exception {
        whenThenVerifyParametrizedInternal(BROKEN_BEARER_HEADER, 1, 1);
    }

    private void whenThenVerifyParametrizedInternal(final String authHeaderValue,
                                                    final int filterNumberOfInvocations,
                                                    final int requestNumberOfInvocations)
            throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeaderValue);
        // when
        auditLoggingFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        // then
        verify(filterChain, times(filterNumberOfInvocations))
                .doFilter(httpServletRequest, httpServletResponse);
        verify(auditLoggingService, times(requestNumberOfInvocations))
                .loggingRequest(httpServletRequest, httpServletResponse);
    }

}
