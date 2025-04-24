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

import org.qubership.atp.integration.configuration.filters.MdcHttpFilter;
import org.qubership.atp.integration.configuration.helpers.HttpRequestParseHelper;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.helpers.StompHelper;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingHelpersConfiguration {

    @Value("${atp.logging.business.keys:userId,projectId,executionRequestId,testRunId,bvTestRunId,bvTestCaseId,"
            + "environmentId,systemId,subscriberId,tsgSessionId,svpSessionId,dataSetId,dataSetListId,attributeId,"
            + "itfLiteRequestId,reportType,itfSessionId,itfContextId,callChainId}")
    private String businessIds;

    @Bean
    @Qualifier("businessIdsString")
    public String businessIdsString() {
        return businessIds;
    }

    @Bean
    public HttpRequestParseHelper httpRequestParseHelper() {
        return new HttpRequestParseHelper();
    }

    @Bean
    public JwtParseHelper jwtParseHelper() {
        return new JwtParseHelper();
    }

    @Bean
    public StompHelper stompHelper() {
        return new StompHelper();
    }

    @Bean
    public Filter mdcHttpFilter(JwtParseHelper jwtParseHelper) {
        return new MdcHttpFilter(jwtParseHelper, MdcUtils.convertIdNamesToList(businessIds));
    }
}
