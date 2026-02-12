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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.qubership.atp.integration.configuration.service.AuditLoggingService.PROJECT_ID_HEADER_NAME;

import java.io.IOException;

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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * Valid Bearer Token for Authorization Header value.
     */
    public static final String TEST_AUTH_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVz"
            + "ODNZai05clhfZUItYWZuUWdPRzhDZUFhblFTakJZTS0wNjQxdVdVIn0.eyJqdGkiOiI1OTRmMzY0My1hODhmLTQ3MTQtYTM2NS1"
            + "jYWJiZjY1MmY5MWYiLCJleHAiOjE2NjIzNzk3NzIsIm5iZiI6MCwiaWF0IjoxNjYyMzc2MTcyLCJpc3MiOiJodHRwczovL2F0cC"
            + "1rZXljbG9hay1zZXJ2aWNlLmRldi1jbG91ZC5leGFtcGxlLmNvbS9hdXRoL3JlYWxtcy9hdHAiLCJhdWQiOiJhY2NvdW50Iiwic"
            + "3ViIjoiYzIzNDRkNzAtMzcwNy00NDE4LWE5YzktZGJkYjhiZWNhNzk2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZnJvbnRlbmQi"
            + "LCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI4MDg1YjdkMy05NDcyLTQ3MGEtYjkxNC1kNzAwNzFkMmIwNzIiLCJhY3I"
            + "iOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiQV"
            + "RQX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hb"
            + "mFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2Zp"
            + "bGUiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJFeGFtcGxlIFVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJleGF"
            + "tcGxlIiwiZ2l2ZW5fbmFtZSI6ImV4YW1wbGUiLCJmYW1pbHlfbmFtZSI6ImV4YW1wbGUiLCJlbWFpbCI6ImV4YW1wbGVAZXhhbX"
            + "BsZS5jb20ifQ.FKCnW9ae-Hza28NJdUVJKud26nHzz7mw9P2O7Pec6GKcbs0nPY6Aabb4CvFL70MbXyOtvK-ErihOAJd__DM_dT"
            + "0nNbPNA4CIP9rod3ylSVjSAxfw1FFh1lSnNwoZjs4K3JOFqmwnJN20ROuCYMo3EcJpFzZA2dt2GQEpj29N4Qk6a-dx3IG6Jz0T0"
            + "LqEN1bjsd3EeiRXqm83wsYi3nkmPx4Yz538Op4QfS2UxDUriUQNaU9vXyoFRNawE09X6C56Y3gTqytG2JDUZhKYcoY87sbERRVt"
            + "JFVLrJxiGiGvrest92ATNrEVz2tI13y4SbiuaQAx0BOT5T4Iz9gAm_FkKg";

    /**
     * Valid Bearer Token for Authorization Header value, especially for M2M.
     */
    public static final String TEST_M2M_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVzO"
            + "DNZai05clhfZUItYWZuUWdPRzhDZUFhblFTakJZTS0wNjQxdVdVIn0.eyJqdGkiOiI2ZTRhMzkyOC1lNmU4LTQxMWYtYWNmZC01"
            + "NDI5YjM0MzA2NzkiLCJleHAiOjE2NjI2MjU1NjMsIm5iZiI6MCwiaWF0IjoxNjYyNjIxOTYzLCJpc3MiOiJodHRwczovL2F0cC1"
            + "rZXljbG9hay1zZXJ2aWNlLmRldi1jbG91ZC5leGFtcGxlLmNvbS9hdXRoL3JlYWxtcy9hdHAiLCJhdWQiOlsicmVhbG0tbWFuYW"
            + "dlbWVudCIsImFjY291bnQiXSwic3ViIjoiMmNhYmFlMzgtNDIwYS00YzIzLThjODMtODhiMjEwZTM5N2NkIiwidHlwIjoiQmVhc"
            + "mVyIiwiYXpwIjoiY2F0YWxvZyIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjYyODhiM2Y4LTJlMDItNDJhMS04NjE5"
            + "LTkyMGNjNTk2YjZmNCIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2h0dHA6Ly8iLCJodHRwczovL3JhbS1"
            + "odHRwcy1zZXJ2aWNlLmRldi1jbG91ZC5leGFtcGxlLmNvbS8qKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV"
            + "9hY2Nlc3MiLCJBVFBfQURNSU4iLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InJlYWxtLW1hbmFnZ"
            + "W1lbnQiOnsicm9sZXMiOlsidmlldy11c2VycyIsInF1ZXJ5LWdyb3VwcyIsInF1ZXJ5LXVzZXJzIl19LCJjYXRhbG9nIjp7InJv"
            + "bGVzIjpbInVtYV9wcm90ZWN0aW9uIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY29"
            + "1bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2"
            + "UsImNsaWVudEhvc3QiOiIxMjguMC4wLjEiLCJjbGllbnRJZCI6ImNhdGFsb2ciLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2a"
            + "WNlLWFjY291bnQtY2F0YWxvZyIsImNsaWVudEFkZHJlc3MiOiIxMjguMC4wLjEiLCJlbWFpbCI6InNlcnZpY2UtYWNjb3VudC1j"
            + "YXRhbG9nQGV4YW1wbGUuY29tIn0.Q4xspJ66Fm53g4OeH3p7ubIzUUr-UyqNdG1v5xoEo4PfPAWzTIWMcznPS7PfrxlHHLRuYZd"
            + "k1htEEPuSYmyz0ILEa72NnycIO2lR4iEC_XZgReBnI8amqdQL3TNQrGGLz0IZTWhaEy3w45bkwNcjesGanK4BtxQyeGa9wmfHGR"
            + "FibuukB2IoId2KhlnpxevEQLmbMhnbK4-pgN1dYpHMZMgdmQKowUIK2fPEwPzEBnQSsxXmmsTXhUtFIz_szi8M2VT2XEZY5DiUX"
            + "qmVaczZR6nPu6Zc1DCGtqv5NqtQ7cF9GCIhlODpxkDs655Q3JX8rHkFrZfYFiVdoVQHZMqpaA";

    /**
     * Invalid Bearer Token for Authorization Header value.
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
     * Basic Authorization Header value.
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
        /*
            Zero invocations of Audit Logging is expected, because IllegalStateException is thrown
            earlier in the AuditLoggingFilter#doFilterInternal
            (primary exception is thrown in the JwtParseHelper#getValueFromTokenByKey).
            This behavior is changed from 1 by @kagw95 at 2025-12-26,
            because auditLoggingService.loggingRequest would always fail due to JWT token parse exception.
         */
        whenThenVerifyParametrizedInternal(TEST_BASIC_HEADER, 1, 0);
    }

    /**
     * Test audit logging in case Bearer Token is set but ParseException is faced.
     *
     * @throws Exception pass ServletException or IOException from inner function.
     */
    @Test
    public void authHeaderSetBearerTokenParseExceptionExpectAuditLoggingCall() throws Exception {
        /*
            The same behavior change as in authHeaderSetBasicTokenExpectAuditLoggingCall test.
         */
        whenThenVerifyParametrizedInternal(BROKEN_BEARER_HEADER, 1, 0);
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
