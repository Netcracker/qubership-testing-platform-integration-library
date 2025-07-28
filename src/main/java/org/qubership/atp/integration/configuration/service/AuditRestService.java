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

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.qubership.atp.integration.configuration.feign.AuditRestFeignClient;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.model.AuditMessage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for REST-based audit logging.
 * Builds audit messages from HTTP request/response and sends them via REST API.
 */
@Slf4j
@RequiredArgsConstructor
public class AuditRestService {

    public static final String PROJECT_ID_HEADER_NAME = "X-Project-Id";

    @Value("${spring.application.name}")
    private String serviceName;

    private final AuditRestFeignClient auditRestFeignClient;
    private final JwtParseHelper jwtParseHelper;
    private final HttpRequestParseHelper httpRequestParseHelper;

    /**
     * Logs request via REST audit service.
     *
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     */
    public void loggingRequest(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Trying to log request via REST");
        try {
            final String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String url = request.getRequestURI();
            final UUID id = UUID.randomUUID();
            final UUID sessionId = jwtParseHelper.getSessionIdFromToken(authToken);
            final UUID projectId = httpRequestParseHelper.getRequestUuidHeader(request, PROJECT_ID_HEADER_NAME, false);
            final String username = jwtParseHelper.getUsernameFromToken(authToken);
            final UUID userId = jwtParseHelper.getUserIdFromToken(authToken);
            final Timestamp startDate = new Timestamp(System.currentTimeMillis());
            final String httpMethod = request.getMethod();
            final String refererPage = request.getHeader("referer");
            final String ipAddress = request.getRemoteAddr();
            final String userAgent = httpRequestParseHelper.getBrowserAgent(request.getHeader("User-Agent"));
            final int httpStatusCode = response.getStatus();

            // Get user action from MDC context (set by AuditRestAspect)
            Map<String, String> mdcMap = MDC.getCopyOfContextMap();
            if (mdcMap == null) {
                mdcMap = new HashMap<>();
            }

            AuditMessage auditMessage = AuditMessage.builder()
                    .id(id.toString())
                    .sessionId(defaultString(sessionId != null ? sessionId.toString() : null, "null"))
                    .projectId(defaultString(projectId != null ? projectId.toString() : null, "null"))
                    .service(defaultString(serviceName, "null"))
                    .username(defaultString(username, "null"))
                    .userId(defaultString(userId != null ? userId.toString() : null, "null"))
                    .url(defaultString(url, "null"))
                    .startDate(startDate.getTime())
                    .httpMethod(defaultString(httpMethod, "null"))
                    .referPage(defaultString(refererPage, "null"))
                    .ipAddress(defaultString(ipAddress, "null"))
                    .userAgent(defaultString(userAgent, "null"))
                    .userAction(defaultString(mdcMap.get("userAction"), "null"))
                    .httpStatusCode(httpStatusCode)
                    .build();

            // Only send if userAction is present
            if (!"null".equals(auditMessage.getUserAction())) {
                auditRestFeignClient.sendAuditMessage(auditMessage);
                log.debug("Request has been successfully logged via REST");
            }
        } catch (Exception err) {
            log.error("Failed to log request via REST", err);
        }
    }
}