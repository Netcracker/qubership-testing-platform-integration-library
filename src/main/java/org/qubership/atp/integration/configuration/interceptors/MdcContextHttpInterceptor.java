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

package org.qubership.atp.integration.configuration.interceptors;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepts all requests, get 'X-Request-Id' header value and set it into {@link MDC} context and corresponding
 * response header.
 *
 * <p>Added 'Zipkin-Trace-Id' to response header in case of Tracer bean exists in context
 *
 * @see MDC
 */
@Slf4j
public class MdcContextHttpInterceptor extends HandlerInterceptorAdapter {

    /**
     * Header Name for ZIPKIN_TRACE_ID values.
     */
    private static final String ZIPKIN_TRACE_ID_HEADER_NAME = "Zipkin-Trace-Id";

    /**
     * Tracer bean.
     */
    private final Tracer tracer;

    /**
     * JwtParseHelper bean.
     */
    private final JwtParseHelper jwtParseHelper;

    /**
     * List of String business IDs.
     */
    private final List<String> businessIds;

    /**
     * Create and configure request ids handler.
     *
     * @param tracer Tracer bean
     * @param jwtParseHelper JwtParseHelper bean
     * @param businessIdsString String with List of business IDs separated by comma.
     */
    public MdcContextHttpInterceptor(final Tracer tracer,
                                     final JwtParseHelper jwtParseHelper,
                                     final String businessIdsString) {
        this.jwtParseHelper = jwtParseHelper;
        this.tracer = tracer;
        this.businessIds = MdcUtils.convertIdNamesToList(businessIdsString);
    }

    /**
     * Pre-Handle HttpServletResponse.
     *
     * @param request HttpServletRequest to process
     * @param response HttpServletResponse to process
     * @param handler Object handler
     * @return Always returns true (successful processing).
     */
    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        processBusinessIds(request);
        Optional.ofNullable(tracer).map(Tracer::currentSpan)
                .map(Span::context)
                .map(TraceContext::traceIdString)
                .ifPresent(traceId -> {
                    log.debug("Add traceId {} to response headers", traceId);
                    response.setHeader(ZIPKIN_TRACE_ID_HEADER_NAME, traceId);
                });
        return true;
    }

    private void processBusinessIds(final HttpServletRequest request) {
        if (!CollectionUtils.isEmpty(businessIds)) {
            for (String idName: businessIds) {
                if (MDC.get(idName) == null) {
                    processPathVariables(request, idName);
                    processRequestParameters(request, idName);
                }
            }
        }
    }

    private void processPathVariables(final HttpServletRequest request, final String idName) {
        Map<Object, Object> pathVariables =
                (Map<Object, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String name = idName.trim();
        if (pathVariables != null) {
            Object value = pathVariables.get(name);
            if (value != null) {
                MdcUtils.put(name, (String) value);
            }
        }
    }

    private void processRequestParameters(final HttpServletRequest request, final String idName) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String name = idName.trim();
        if (parameterMap != null) {
            String[] value = parameterMap.get(name);
            if (!StringUtils.isEmpty(value)) {
                MdcUtils.put(name, String.join(",", value));
            }
        }
    }

    /**
     * After-Completion Handler (remove all businessIds from MDC).
     *
     * @param request HttpServletRequest to process
     * @param response HttpServletResponse to process
     * @param handler Object handler
     * @param ex Exception possibly thrown during early processing.
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) {
        if (!CollectionUtils.isEmpty(businessIds)) {
            businessIds.forEach(MDC::remove);
        }
    }
}
