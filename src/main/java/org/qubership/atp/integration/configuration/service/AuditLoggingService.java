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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.http.HttpHeaders;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.protos.KafkaAuditLoggingMessage.AuditLoggingMessage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuditLoggingService {

    /**
     * Name of header containing project id.
     */
    public static final String PROJECT_ID_HEADER_NAME = "X-Project-Id";

    /**
     * Service Name.
     */
    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * Audit Logging Topic Name.
     */
    @Value("${atp.audit.logging.topic.name}")
    private String topic;

    /**
     * Producer bean.
     */
    private final Producer<UUID, AuditLoggingMessage> auditLoggingKafkaProducer;

    /**
     * JwtParseHelper bean.
     */
    private final JwtParseHelper jwtParseHelper;

    /**
     * HttpRequestParseHelper bean.
     */
    private final HttpRequestParseHelper httpRequestParseHelper;

    /**
     * Logging of request.
     *
     * @param request HttpServletRequest to process
     * @param response HttpServletResponse to process.
     */
    public void loggingRequest(final HttpServletRequest request, final HttpServletResponse response) {
        log.debug("Trying to log request");
        try {
            final String url = request.getRequestURI();
            final UUID id = UUID.randomUUID();
            final UUID projectId = httpRequestParseHelper.getRequestUuidHeader(request, PROJECT_ID_HEADER_NAME, false);
            final String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);

            final UUID sessionId;
            final String username;
            final UUID userId;
            Map<String, ?> tokenDataMap = jwtParseHelper.getTokenDataMapFromToken(authToken);
            if (tokenDataMap != null) {
                sessionId = jwtParseHelper.getSessionIdFromTokenDataMap(tokenDataMap);
                username = jwtParseHelper.getUsernameFromTokenDataMap(tokenDataMap);
                userId = jwtParseHelper.getUserIdFromTokenDataMap(tokenDataMap);
            } else {
                sessionId = null;
                username = null;
                userId = null;
            }
            if (userId == null || sessionId == null) {
                throw new IllegalStateException("UserId and/or SessionId (parsed from token) are null");
            }

            final Timestamp startDate = new Timestamp(System.currentTimeMillis());
            final String httpMethod = request.getMethod();
            final String refererPage = request.getHeader("referer");
            final String ipAddress = request.getRemoteAddr();
            final String userAgent = httpRequestParseHelper.getBrowserAgent(request.getHeader("User-Agent"));
            final int httpStatusCode = response.getStatus();

            Map<String, String> mdcMap = MDC.getCopyOfContextMap();
            if (mdcMap == null) {
                mdcMap = new HashMap<>();
            }

            AuditLoggingMessage message = AuditLoggingMessage.newBuilder()
                    .setId(id.toString())
                    .setSessionId(Objects.toString(sessionId, "null"))
                    .setProjectId(Objects.toString(projectId, "null"))
                    .setService(Objects.toString(serviceName, "null"))
                    .setUsername(Objects.toString(username, "null"))
                    .setUserId(userId.toString())
                    .setUrl(Objects.toString(url, "null"))
                    .setStartDate(startDate.getTime())
                    .setHttpMethod(Objects.toString(httpMethod, "null"))
                    .setReferPage(Objects.toString(refererPage, "null"))
                    .setIpAddress(Objects.toString(ipAddress, "null"))
                    .setUserAgent(userAgent)
                    .setUserAction(Objects.toString(mdcMap.get("userAction"), "null"))
                    .setHttpStatusCode(httpStatusCode)
                    .build();

            if (!message.getUserAction().equals("null")) {
                ProducerRecord<UUID, AuditLoggingMessage> record = new ProducerRecord<>(topic, sessionId, message);
                auditLoggingKafkaProducer.send(record);
                log.debug("Request have been successfully logged");
            }
        } catch (Exception err) {
            log.error("Failed to log request", err);
        }
    }
}
