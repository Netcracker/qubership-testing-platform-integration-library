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

package org.qubership.atp.integration.configuration.filters;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.mdc.MdcField;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcHttpFilter extends OncePerRequestFilter {

    /**
     * JwtParseHelper bean.
     */
    private final JwtParseHelper jwtParseHelper;

    /**
     * List of String business IDs.
     */
    private final List<String> businessIds;

    /**
     * Handler to perform auth token processing of request.
     *
     * @param request HttpServletRequest received
     * @param response HttpServletResponse to be sent
     * @param filterChain Chain of filters
     * @throws ServletException in case Servlet processing errors
     * @throws IOException in case request/response processing IO errors.
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        MDC.clear();
        businessIds.forEach(idName -> processHeaders(request, idName));
        processUserId(request);
        filterChain.doFilter(request, response);
    }

    private void processHeaders(final HttpServletRequest request, final String idName) {
        MdcUtils.put(idName, MdcUtils.getHeaderFromRequest(request, MdcUtils.convertIdNameToHeader(idName)));
    }

    private void processUserId(final HttpServletRequest request) {
        final String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authToken != null && StringUtils.startsWithIgnoreCase(authToken, "Bearer ")) {
            try {
                final UUID userId = jwtParseHelper.getUserIdFromNonM2MToken(authToken);
                if (userId != null) {
                    MdcUtils.put(MdcField.USER_ID.toString(), userId);
                }
            } catch (Exception e) {
                log.error("Error while getting userId from token", e);
            }
        }
    }
}
