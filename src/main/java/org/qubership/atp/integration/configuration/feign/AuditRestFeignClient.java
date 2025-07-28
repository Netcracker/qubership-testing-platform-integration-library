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

package org.qubership.atp.integration.configuration.feign;

import org.qubership.atp.integration.configuration.model.AuditMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for REST-based audit logging service.
 */
@FeignClient(
        name = "${feign.atp.audit.logging.rest.name:atp-audit-service}",
        url = "${feign.atp.audit.logging.rest.url}"
)
public interface AuditRestFeignClient {

    /**
     * Send audit message to REST audit service.
     *
     * @param auditMessage the audit message to send
     */
    @PostMapping("${feign.atp.audit.logging.rest.route:/api/v1/audit}")
    void sendAuditMessage(@RequestBody AuditMessage auditMessage);
}