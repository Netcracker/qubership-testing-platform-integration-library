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

import jakarta.servlet.ServletException;

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

    /**
     * Mdc Context Http Interceptor.
     */
    private MdcContextHttpInterceptor interceptor;

    /**
     * Mdc Http Filter.
     */
    private MdcHttpFilter filter;

    /**
     * Mock Http Servlet Request.
     */
    private MockHttpServletRequest request;

    /**
     * Setup interceptor, filter and request before tests.
     */
    @Before
    public void setUp() {
        interceptor = new MdcContextHttpInterceptor(null, new JwtParseHelper(), "projectId");
        filter = new MdcHttpFilter(new JwtParseHelper(), Collections.singletonList("projectId"));
        request = new MockHttpServletRequest("GET", "/api/example");
    }

    /**
     * Test that loggingInterceptor is applied to request, so MDC contains projectId and userId fields.
     *
     * @throws ServletException in case some servlet processing exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void loggingInterceptorShouldBeApplied() throws ServletException, IOException {
        request.addHeader(PROJECT_ID_HEADER_NAME, UUID.randomUUID());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.TEST_AUTH_HEADER);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNotNull(MDC.get(MdcField.PROJECT_ID.toString()));
        Assert.assertNotNull(MDC.get(MdcField.USER_ID.toString()));
    }

    /**
     * Test that MDC doesn't contain userId in case interceptor is applied to a request without AUTHORIZATION header.
     *
     * @throws ServletException in case some servlet processing exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void loggingInterceptorUserIdIsAbsent() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

    /**
     * Test that MDC doesn't contain projectId in case interceptor is applied to a request without
     * PROJECT_ID_HEADER_NAME header.
     *
     * @throws ServletException in case some servlet processing exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void loggingInterceptorProjectIdIsAbsent() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.PROJECT_ID.toString()));
    }

    /**
     * Test that userId field isn't added to MDC in case AUTHORIZATION header contains M2M token.
     *
     * @throws ServletException in case some servlet processing exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void loggingInterceptorTokenIsM2M() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.TEST_M2M_HEADER);
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

    /**
     * Test that userId field isn't added to MDC in case AUTHORIZATION header contains Basic token.
     *
     * @throws ServletException in case some servlet processing exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void loggingInterceptorTokenIsNotBearer() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.TEST_BASIC_HEADER);
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

    /**
     * Test that userId field isn't added to MDC in case AUTHORIZATION header contains broken Bearer token.
     *
     * @throws ServletException in case some servlet processing exceptions
     * @throws IOException in case IO exceptions.
     */
    @Test
    public void loggingInterceptorBrokenBearerToken() throws ServletException, IOException {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        request.addHeader(HttpHeaders.AUTHORIZATION, AuditLoggingFilterTest.BROKEN_BEARER_HEADER);
        interceptor.preHandle(request, new MockHttpServletResponse(), null);
        Assert.assertNull(MDC.get(MdcField.USER_ID.toString()));
    }

}
