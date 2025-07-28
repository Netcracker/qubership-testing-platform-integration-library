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

package org.qubership.atp.integration.configuration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Audit message model for REST-based audit logging.
 * Contains the same fields as Kafka AuditLoggingMessage for consistency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditMessage {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("service")
    private String service;

    @JsonProperty("username")
    private String username;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("startDate")
    private long startDate;

    @JsonProperty("httpMethod")
    private String httpMethod;

    @JsonProperty("referPage")
    private String referPage;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("userAgent")
    private String userAgent;

    @JsonProperty("userAction")
    private String userAction;

    @JsonProperty("httpStatusCode")
    private int httpStatusCode;
}