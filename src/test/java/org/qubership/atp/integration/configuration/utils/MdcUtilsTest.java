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

package org.qubership.atp.integration.configuration.utils;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;

public class MdcUtilsTest {

    @Before
    public void setup() {
        MDC.clear();
    }


    @Test
    public void test_putUUIDObject_successful() {
        MdcUtils.put("key", UUID.randomUUID());
        Assert.assertNotNull(MDC.get("key"));
    }

    @Test
    public void test_putStringObject_successful() {
        MdcUtils.put("key", UUID.randomUUID().toString());
        Assert.assertNotNull(MDC.get("key"));

    }

    @Test
    public void test_convertIdNameToHeaderName_successful() {
        Assert.assertEquals(MdcUtils.convertIdNameToHeader("executionRequestId"), "X-Execution-Request-Id");
        Assert.assertEquals(MdcUtils.convertIdNameToHeader("projectId"), "X-Project-Id");
    }

    @Test
    public void test_putNullUUIDObject_successful() {
        MdcUtils.put("key", (UUID) null);
        Assert.assertNull(MDC.get("key"));
    }

}
