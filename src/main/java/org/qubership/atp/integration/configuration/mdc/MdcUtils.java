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

package org.qubership.atp.integration.configuration.mdc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

public class MdcUtils {

    /**
     * Validate and put UUID in MDC.
     */
    public static boolean put(String key, UUID value) {
        if (value != null) {
            return put(key, value.toString());
        }
        return false;
    }

    /**
     * Validate and put String in MDC.
     */
    public static boolean put(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            MDC.put(key, value);
            return true;
        }
        return false;
    }

    /**
     * Convert id name in header name.
     */
    public static String convertIdNameToHeader(String idName) {
        return "X-" + Arrays.stream(idName.split("(?=\\p{Upper})"))
                .map(org.springframework.util.StringUtils::capitalize)
                .collect(Collectors.joining("-"));
    }

    /**
     * Returns header from request ignoring name case.
     */
    public static String getHeaderFromRequest(HttpServletRequest request, String headerName) {
        if (request != null && request.getHeaderNames() != null) {
            for (String originalHeaderName : Collections.list(request.getHeaderNames())) {
                if (originalHeaderName.equalsIgnoreCase(headerName)) {
                    return request.getHeader(originalHeaderName);
                }
            }
        }
        return null;
    }

    /**
     * Convert id names to list of strings.
     */
    public static List<String> convertIdNamesToList(String businessIdsString) {
        return StringUtils.isNotBlank(businessIdsString)
                ? Arrays.stream(businessIdsString.split(","))
                .map(String::trim).collect(Collectors.toList())
                : Collections.emptyList();
    }


    /**
     * Set MDC for the current thread.
     */
    public static void setContextMap(Map<String, String> mdcMap) {
        MDC.setContextMap(CollectionUtils.isEmpty(mdcMap) ? new HashMap<>() : mdcMap);
    }
}
