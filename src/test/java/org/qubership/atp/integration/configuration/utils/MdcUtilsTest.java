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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;

public class MdcUtilsTest {

    /**
     * Clear MDC before tests.
     */
    @Before
    public void setup() {
        MDC.clear();
    }

    /**
     * Clear MDC after tests.
     */
    @After
    public void cleanup() {
        MDC.clear();
    }

    /**
     * Test of putting UUID value to MDC.
     */
    @Test
    public void testPutUUIDObjectSuccessful() {
        MdcUtils.put("key", UUID.randomUUID());
        Assert.assertNotNull(MDC.get("key"));
    }

    /**
     * Test of putting String value to MDC.
     */
    @Test
    public void testPutStringObjectSuccessful() {
        MdcUtils.put("key", UUID.randomUUID().toString());
        Assert.assertNotNull(MDC.get("key"));
    }

    /**
     * Test of converting MDC business IDs to the corresponding header names.
     */
    @Test
    public void testConvertIdNameToHeaderNameSuccessful() {
        Assert.assertEquals("X-Execution-Request-Id", MdcUtils.convertIdNameToHeader("executionRequestId"));
        Assert.assertEquals("X-Project-Id", MdcUtils.convertIdNameToHeader("projectId"));
    }

    /**
     * Test of putting null UUID value to MDC.
     */
    @Test
    public void testPutNullUUIDObjectSuccessful() {
        MdcUtils.put("key", (UUID) null);
        Assert.assertNull(MDC.get("key"));
    }

}
