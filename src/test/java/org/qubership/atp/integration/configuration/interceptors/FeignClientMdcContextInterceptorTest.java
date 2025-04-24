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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.integration.configuration.mdc.MdcField;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;

import feign.RequestTemplate;

public class FeignClientMdcContextInterceptorTest {

    private FeignClientMdcContextInterceptor interceptor;
    private RequestTemplate request;


    @Before
    public void setUp() {
        interceptor = new FeignClientMdcContextInterceptor("projectId");
        request = new RequestTemplate();
    }

    @Test
    public void logging_interceptor_shouldBeApplied() {
        MdcUtils.put(MdcField.PROJECT_ID.toString(), UUID.randomUUID());
        interceptor.apply(request);
        Assert.assertTrue(request.headers().containsKey(MdcUtils.convertIdNameToHeader(MdcField.PROJECT_ID.toString())));
    }


}
