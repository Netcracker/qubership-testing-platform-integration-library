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

package org.qubership.atp.integration.configuration.helpers;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRequestParseHelper {

    /**
     * Get and transform to UUID selected request header.
     *
     * @param request    http servlet request
     * @param headerName header name
     * @param required   throw IllegalArgumentException if request header required and value is absent
     * @return request header UUID value
     */
    public UUID getRequestUuidHeader(HttpServletRequest request, String headerName, boolean required) {
        String headerValue = request.getHeader(headerName);

        if (!StringUtils.isEmpty(headerValue)) {
            return UUID.fromString(headerValue);
        } else if (required) {
            log.error("Required header '{}' value is empty", headerName);
            throw new IllegalArgumentException("Required header '" + headerName + "' is empty");
        } else {
            return null;
        }
    }

    /**
     * Parse on browser agent.
     */
    public String getBrowserAgent(String userAgent) {
        String browserAgent = "unknown browser";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("edg")) {
            browserAgent = "Edge";
        } else if (userAgent.contains("firefox")) {
            browserAgent = "Mozilla";
        } else if (userAgent.contains("opera") || userAgent.contains("presto") || userAgent.contains("opr")) {
            browserAgent = "Opera";
        } else if (userAgent.contains("apple") && userAgent.contains("safari") && userAgent.contains("mobile")) {
            browserAgent = "Safari";
        } else if (userAgent.contains("msie")) {
            browserAgent = "EI";
        } else if (userAgent.contains("chrome")) {
            browserAgent = "Chrome";
        } else if (userAgent.contains("gecko")) {
            browserAgent = "browser on core Gecko";
        } else if (userAgent.contains("konqueror")) {
            browserAgent = "Browser on core Konqueror";
        }

        return browserAgent;
    }
}
