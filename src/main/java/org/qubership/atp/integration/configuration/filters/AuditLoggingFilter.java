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

import static org.springframework.util.StringUtils.isEmpty;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.service.AuditLoggingService;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuditLoggingFilter extends OncePerRequestFilter {

    /**
     * AuditLoggingService bean.
     */
    private final AuditLoggingService auditLoggingService;

    /**
     * JwtParseHelper bean.
     */
    private final JwtParseHelper jwtParseHelper;

    /**
     * Handler to perform audit logging of request.
     *
     * @param request HttpServletRequest received
     * @param response HttpServletResponse to be sent
     * @param filterChain Chain of filters
     * @throws ServletException in case Servlet processing errors
     * @throws IOException in case request/response processing IO errors.
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        log.debug("Intercept request for audit logging");

        final String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String url = request.getRequestURI();
        Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        if (copyOfContextMap != null) {
            copyOfContextMap.remove("userAction");
        }
        log.debug("Continue request filter chain");
        filterChain.doFilter(request, response);
        if (isEmpty(authToken)) {
            log.debug("Audit logging was skipped for the '{}' request because of empty Authorization token", url);
        } else {
            boolean isM2Mtoken;
            try {
                isM2Mtoken = jwtParseHelper.isM2Mtoken(authToken);
            } catch (Exception e) {
                log.error("Error while checking token type", e);
                isM2Mtoken = false;
            }
            if (isM2Mtoken) {
                log.debug("Audit logging was skipped for the '{}' request because of M2M Authorization token", url);
            } else {
                auditLoggingService.loggingRequest(request, response);
            }
        }
    }
}
