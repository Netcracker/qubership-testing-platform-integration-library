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

import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.helpers.StompHelper;
import org.qubership.atp.integration.configuration.interceptors.FeignClientMdcContextInterceptor;
import org.qubership.atp.integration.configuration.interceptors.MdcChannelInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MdcInterceptorsHelperConfiguration {

    @Bean
    public FeignClientMdcContextInterceptor feignClientMdcContextInterceptor(
            @Qualifier("businessIdsString") String businessIds) {
        return new FeignClientMdcContextInterceptor(businessIds);
    }

    @Bean
    public MdcChannelInterceptor mdcChannelInterceptor(StompHelper stompHelper,
                                                       JwtParseHelper jwtParseHelper,
                                                       @Qualifier("businessIdsString") String businessIds) {
        return new MdcChannelInterceptor(stompHelper, jwtParseHelper, businessIds);
    }
}
