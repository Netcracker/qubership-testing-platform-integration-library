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

import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.interceptors.MdcContextHttpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import brave.Tracer;

@Configuration
public class HttpInterceptorConfiguration implements WebMvcConfigurer {

    /**
     * Tracer bean.
     */
    private final Tracer tracer;

    /**
     * HttpRequestParseHelper bean.
     */
    private final HttpRequestParseHelper httpRequestParseHelper;

    /**
     * JwtParseHelper bean.
     */
    private final JwtParseHelper jwtParseHelper;

    /**
     * String list of business IDs delimited with comma.
     */
    private final String businessIds;

    /**
     * Create and configure mvc configuration.
     *
     * @param tracer Tracer bean
     * @param httpRequestParseHelper HttpRequestParseHelper bean
     * @param jwtParseHelper JwtParseHelper bean
     * @param businessIds String list of business IDs delimited with comma.
     */
    public HttpInterceptorConfiguration(@Autowired(required = false) final Tracer tracer,
                                        final HttpRequestParseHelper httpRequestParseHelper,
                                        final JwtParseHelper jwtParseHelper,
                                        @Qualifier("businessIdsString") final String businessIds) {
        this.httpRequestParseHelper = httpRequestParseHelper;
        this.jwtParseHelper = jwtParseHelper;
        this.tracer = tracer;
        this.businessIds = businessIds;
    }

    /**
     * Add interceptors to interceptor registry; only MdcContextHttpInterceptor is added currently.
     *
     * @param registry InterceptorRegistry
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new MdcContextHttpInterceptor(tracer, jwtParseHelper, businessIds));
    }
}
