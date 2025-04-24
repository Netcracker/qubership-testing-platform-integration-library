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

package org.qubership.atp.integration.configuration.interceptors;

import static org.qubership.atp.integration.configuration.service.AuditLoggingService.PROJECT_ID_HEADER_NAME;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.http.HttpHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.integration.configuration.filters.AuditLoggingFilterTest;
import org.qubership.atp.integration.configuration.filters.MdcHttpFilter;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.mdc.MdcField;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class MdcHttpPreHandleTest {
    public static final String TEST_AUTH_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVzOD" +
            "NZai05clhfZUItYWZuUWdPRzhDZUFhblFTakJZTS0wNjQxdVdVIn0.eyJqdGkiOiI1OTRmMzY0My1hODhmLTQ3MTQtYTM2NS1jYWJiZj" +
            "Y1MmY5MWYiLCJleHAiOjE2NjIzNzk3NzIsIm5iZiI6MCwiaWF0IjoxNjYyMzc2MTcyLCJpc3MiOiJodHRwczovL2F0cC1rZXljbG9hay" +
            "1kZXYyMjIuZGV2LWF0cC1jbG91ZC5uZXRjcmFja2VyLmNvbS9hdXRoL3JlYWxtcy9hdHAyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6Im" +
            "MyMzQ0ZDcwLTM3MDctNDQxOC1hOWM5LWRiZGI4YmVjYTc5NiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImZyb250ZW5kIiwiYXV0aF90aW" +
            "1lIjowLCJzZXNzaW9uX3N0YXRlIjoiODA4NWI3ZDMtOTQ3Mi00NzBhLWI5MTQtZDcwMDcxZDJiMDcyIiwiYWNyIjoiMSIsImFsbG93ZW" +
            "Qtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsIkFUUF9BRE1JTiIsInVtYV9hdX" +
            "Rob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS" +
            "1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbH" +
            "NlLCJuYW1lIjoiQWRtaW4gQWRtaW5vdmljaCIsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIiwiZ2l2ZW5fbmFtZSI6IkFkbWluIi" +
            "wiZmFtaWx5X25hbWUiOiJBZG1pbm92aWNoIiwiZW1haWwiOiJ0ZXN0Nzc3QHRlc3QifQ.FKCnW9ae-Hza28NJdUVJKud26nHzz7mw9P2" +
            "O7Pec6GKcbs0nPY6Aabb4CvFL70MbXyOtvK-ErihOAJd__DM_dT0nNbPNA4CIP9rod3ylSVjSAxfw1FFh1lSnNwoZjs4K3JOFqmwnJN2" +
            "0ROuCYMo3EcJpFzZA2dt2GQEpj29N4Qk6a-dx3IG6Jz0T0LqEN1bjsd3EeiRXqm83wsYi3nkmPx4Yz538Op4QfS2UxDUriUQNaU9vXyo" +
            "FRNawE09X6C56Y3gTqytG2JDUZhKYcoY87sbERRVtJFVLrJxiGiGvrest92ATNrEVz2tI13y4SbiuaQAx0BOT5T4Iz9gAm_FkKg";

    public static final String TEST_M2M_HEADER = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI5TWVzODN" +
            "Zai05clhfZUItYWZuUWdPRzhDZUFhblFTakJZTS0wNjQxdVdVIn0.eyJqdGkiOiI2ZTRhMzkyOC1lNmU4LTQxMWYtYWNmZC01NDI5YjM" +
            "0MzA2NzkiLCJleHAiOjE2NjI2MjU1NjMsIm5iZiI6MCwiaWF0IjoxNjYyNjIxOTYzLCJpc3MiOiJodHRwczovL2F0cC1rZXljbG9hay1" +
            "kZXYyMjIuZGV2LWF0cC1jbG91ZC5uZXRjcmFja2VyLmNvbS9hdXRoL3JlYWxtcy9hdHAyIiwiYXVkIjpbInJlYWxtLW1hbmFnZW1lbnQ" +
            "iLCJhY2NvdW50Il0sInN1YiI6IjJjYWJhZTM4LTQyMGEtNGMyMy04YzgzLTg4YjIxMGUzOTdjZCIsInR5cCI6IkJlYXJlciIsImF6cCI" +
            "6ImNhdGFsb2ciLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI2Mjg4YjNmOC0yZTAyLTQyYTEtODYxOS05MjBjYzU5NmI2ZjQ" +
            "iLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9odHRwOi8vIiwiaHR0cHM6Ly9yYW0taHR0cHMtZGV2MjIyLmRldi1" +
            "hdHAtY2xvdWQubmV0Y3JhY2tlci5jb20vKioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiQVRQX0F" +
            "ETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJyZWFsbS1tYW5hZ2VtZW50Ijp7InJvbGVzIjpbInZ" +
            "pZXctdXNlcnMiLCJxdWVyeS1ncm91cHMiLCJxdWVyeS11c2VycyJdfSwiY2F0YWxvZyI6eyJyb2xlcyI6WyJ1bWFfcHJvdGVjdGlvbiJ" +
            "dfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl1" +
            "9fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjbGllbnRIb3N0IjoiMTAuMjM2LjE1OC43OSI" +
            "sImNsaWVudElkIjoiY2F0YWxvZyIsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC1jYXRhbG9nIiwiY2xpZW50QWR" +
            "kcmVzcyI6IjEwLjIzNi4xNTguNzkiLCJlbWFpbCI6InNlcnZpY2UtYWNjb3VudC1jYXRhbG9nQHBsYWNlaG9sZGVyLm9yZyJ9.Q4xspJ" +
            "66Fm53g4OeH3p7ubIzUUr-UyqNdG1v5xoEo4PfPAWzTIWMcznPS7PfrxlHHLRuYZdk1htEEPuSYmyz0ILEa72NnycIO2lR4iEC_XZgRe" +
            "BnI8amqdQL3TNQrGGLz0IZTWhaEy3w45bkwNcjesGanK4BtxQyeGa9wmfHGRFibuukB2IoId2KhlnpxevEQLmbMhnbK4-pgN1dYpHMZM" +
            "gdmQKowUIK2fPEwPzEBnQSsxXmmsTXhUtFIz_szi8M2VT2XEZY5DiUXqmVaczZR6nPu6Zc1DCGtqv5NqtQ7cF9GCIhlODpxkDs655Q3J" +
            "X8rHkFrZfYFiVdoVQHZMqpaA";

    private MdcContextHttpInterceptor interceptor;
    private MdcHttpFilter filter;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        interceptor = new MdcContextHttpInterceptor(null, new JwtParseHelper(), "projectId");
        filter = new MdcHttpFilter(new JwtParseHelper(), Collections.singletonList("projectId"));
        request = new MockHttpServletRequest("GET", "/api/example");
    }

    @Test
    public void logging_interceptor_shouldBeApplied() throws ServletException, IOException {
        request.addHeader(PROJECT_ID_HEADER_NAME, UUID.randomUUID());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.TEST_AUTH_HEADER);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNotNull(MDC.get(MdcField.PROJECT_ID.toString()));
        Assert.assertNotNull(MDC.get(MdcField.USER_ID.toString()));
    }

    @Test
    public void logging_interceptor_userIdIsAbsent() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

    @Test
    public void logging_interceptor_projectIdIsAbsent() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.PROJECT_ID.toString()));
    }

    @Test
    public void logging_interceptor_tokenIsM2M() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.TEST_M2M_HEADER);
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

    @Test
    public void logging_interceptor_tokenIsNotBearer() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.TEST_BASIC_HEADER);
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

    @Test
    public void logging_interceptor_BrokenBearerToken() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.BROKEN_BEARER_HEADER);
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

}
