/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.atp.integration.configuration.interceptors;

import java.util.List;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;

public class MdcRestTemplateInterceptor implements HttpRequestInterceptor {

    /**
     * List of String business IDs.
     */
    List<String> businessIds;

    /**
     * Constructor.
     *
     * @param businessIds List of String business IDs.
     */
    public MdcRestTemplateInterceptor(final List<String> businessIds) {
        this.businessIds = businessIds;
    }

    /**
     * Process HttpRequest - add headers for all businessIds present in MDC (with non-null values).
     *
     * @param httpRequest HttpRequest to process
     * @param entityDetails EntityDetails object
     * @param httpContext HttpContext object.
     */
    @Override
    public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) {
        businessIds.forEach(idName -> {
            String value = MDC.get(idName);
            if (value != null) {
                httpRequest.addHeader(MdcUtils.convertIdNameToHeader(idName), value);
            }
        });
    }
}
