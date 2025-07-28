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

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.service.AuditRestService;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter for REST-based audit logging.
 * Intercepts HTTP requests and delegates to AuditRestService for logging.
 */
@Slf4j
@RequiredArgsConstructor
public class AuditRestLoggingFilter extends OncePerRequestFilter {

    private final AuditRestService auditRestService;
    private final JwtParseHelper jwtParseHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("Intercept request for REST audit logging");

        final String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String url = request.getRequestURI();

        // Clear any existing userAction from MDC
        Map<String, String> mdcMapCopy = MDC.getCopyOfContextMap();
        if (mdcMapCopy != null) {
            mdcMapCopy.remove("userAction");
        }

        log.debug("Continue request filter chain");
        filterChain.doFilter(request, response);

        // Process audit logging after request completion
        if (authToken == null || authToken.isEmpty()) {
            log.debug("REST audit logging was skipped for '{}' request because of empty Authorization token", url);
        } else {
            boolean isM2Mtoken;
            try {
                isM2Mtoken = jwtParseHelper.isM2Mtoken(authToken);
            } catch (Exception e) {
                log.error("Error while checking token type for REST audit", e);
                isM2Mtoken = false;
            }
            if (isM2Mtoken) {
                log.debug("REST audit logging was skipped for '{}' request because of M2M Authorization token", url);
            } else {
                auditRestService.loggingRequest(request, response);
            }
        }
    }
}