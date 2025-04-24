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

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepts all feign client request and add 'X-Request-Id' header with value from {@link MDC} context.
 * See <a>https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-feign.html</a>.
 *
 * @see MDC
 * @see org.springframework.cloud.openfeign.FeignClient
 */
@Slf4j
public class FeignClientMdcContextInterceptor implements RequestInterceptor {

    private final List<String> businessIds;

    public FeignClientMdcContextInterceptor(String businessIdsString) {
        businessIds = MdcUtils.convertIdNamesToList(businessIdsString);
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!CollectionUtils.isEmpty(businessIds)) {
            businessIds.stream()
                    .filter(idName -> StringUtils.isNotBlank(MDC.get(idName)))
                    .forEach(idName -> template.header(MdcUtils.convertIdNameToHeader(idName), MDC.get(idName)));
        }
    }
}
