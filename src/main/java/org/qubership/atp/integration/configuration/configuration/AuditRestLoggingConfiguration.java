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

package org.qubership.atp.integration.configuration.configuration;

import javax.servlet.Filter;

import org.qubership.atp.integration.configuration.feign.AuditRestFeignClient;
import org.qubership.atp.integration.configuration.filters.AuditRestLoggingFilter;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.service.AuditRestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for REST-based audit logging.
 * Activates when atp.audit.logging.rest.enable=true
 */
@ConditionalOnProperty(value = "atp.audit.logging.rest.enable", havingValue = "true")
@EnableAspectJAutoProxy
@EnableFeignClients(basePackageClasses = AuditRestFeignClient.class)
@ComponentScan("org.qubership.atp.integration.configuration")
public class AuditRestLoggingConfiguration {

    /**
     * REST audit service URL - REQUIRED property.
     */
    @Value("${feign.atp.audit.logging.rest.url}")
    private String auditServiceUrl;

    /**
     * Creates REST audit service with required dependencies.
     */
    @Bean
    public AuditRestService auditRestService(AuditRestFeignClient auditRestFeignClient,
                                             JwtParseHelper jwtHelper,
                                             HttpRequestParseHelper requestHelper) {
        return new AuditRestService(auditRestFeignClient, jwtHelper, requestHelper);
    }

    /**
     * Creates REST audit logging filter.
     */
    @Bean
    public Filter auditRestLoggingFilter(AuditRestService auditRestService, JwtParseHelper jwtHelper) {
        return new AuditRestLoggingFilter(auditRestService, jwtHelper);
    }
}