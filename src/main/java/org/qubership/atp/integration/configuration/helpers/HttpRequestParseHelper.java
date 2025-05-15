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
     * Get request header value by header name and transform it to UUID.
     *
     * @param request    http servlet request
     * @param headerName header name
     * @param required   throw IllegalArgumentException if request header required but value is absent
     * @return request header UUID value
     */
    public UUID getRequestUuidHeader(final HttpServletRequest request,
                                     final String headerName,
                                     final boolean required) {
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
     * Parse of browser agent name.
     *
     * @param userAgent String name of browser sender of the request.
     * @return String brief browser agent name calculated.
     */
    public String getBrowserAgent(final String userAgent) {
        String browserAgent = "unknown browser";
        String agent = userAgent.toLowerCase();
        if (agent.contains("edg")) {
            browserAgent = "Edge";
        } else if (agent.contains("firefox")) {
            browserAgent = "Mozilla";
        } else if (agent.contains("opera") || agent.contains("presto") || agent.contains("opr")) {
            browserAgent = "Opera";
        } else if (agent.contains("apple") && agent.contains("safari") && agent.contains("mobile")) {
            browserAgent = "Safari";
        } else if (agent.contains("msie")) {
            browserAgent = "IE";
        } else if (agent.contains("chrome")) {
            browserAgent = "Chrome";
        } else if (agent.contains("gecko")) {
            browserAgent = "browser on core Gecko";
        } else if (agent.contains("konqueror")) {
            browserAgent = "Browser on core Konqueror";
        }
        return browserAgent;
    }
}
