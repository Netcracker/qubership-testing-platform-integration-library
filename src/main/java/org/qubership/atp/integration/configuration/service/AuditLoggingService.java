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
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.protos.KafkaAuditLoggingMessage.AuditLoggingMessage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuditLoggingService {

    public static final String PROJECT_ID_HEADER_NAME = "X-Project-Id";

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${atp.audit.logging.topic.name}")
    private String topic;

    private final Producer<UUID, AuditLoggingMessage> auditLoggingKafkaProducer;
    private final JwtParseHelper jwtParseHelper;
    private final HttpRequestParseHelper httpRequestParseHelper;

    /**
     * Logging request.
     */
    public void loggingRequest(HttpServletRequest request, HttpServletResponse response) {

        log.debug("Trying to log request");
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
            Map<String, String> mdcMap =
                    MDC.getCopyOfContextMap() == null ? new HashMap<>() : MDC.getCopyOfContextMap();
            AuditLoggingMessage message = AuditLoggingMessage.newBuilder()
                    .setId(id.toString())
                    .setSessionId(defaultString(sessionId.toString(), "null"))
                    .setProjectId(defaultString(projectId.toString(), "null"))
                    .setService(defaultString(serviceName, "null"))
                    .setUsername(defaultString(username, "null"))
                    .setUserId(userId.toString())
                    .setUrl(defaultString(url, "null"))
                    .setStartDate(startDate.getTime())
                    .setHttpMethod(defaultString(httpMethod, "null"))
                    .setReferPage(defaultString(refererPage, "null"))
                    .setIpAddress(defaultString(ipAddress, "null"))
                    .setUserAgent(userAgent)
                    .setUserAction(defaultString(mdcMap.get("userAction"), "null"))
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
