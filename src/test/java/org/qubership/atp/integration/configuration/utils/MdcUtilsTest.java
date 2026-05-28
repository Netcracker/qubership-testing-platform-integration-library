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

package org.qubership.atp.integration.configuration.utils;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;

public class MdcUtilsTest {

    /**
     * Clear MDC before tests.
     */
    @BeforeEach
    public void setup() {
        MDC.clear();
    }

    /**
     * Clear MDC after tests.
     */
    @AfterEach
    public void cleanup() {
        MDC.clear();
    }

    /**
     * Test of putting UUID value to MDC.
     */
    @Test
    public void testPutUUIDObjectSuccessful() {
        MdcUtils.put("key", UUID.randomUUID());
        Assertions.assertNotNull(MDC.get("key"));
    }

    /**
     * Test of putting String value to MDC.
     */
    @Test
    public void testPutStringObjectSuccessful() {
        MdcUtils.put("key", UUID.randomUUID().toString());
        Assertions.assertNotNull(MDC.get("key"));
    }

    /**
     * Test of converting MDC business IDs to the corresponding header names.
     */
    @Test
    public void testConvertIdNameToHeaderNameSuccessful() {
        Assertions.assertEquals("X-Execution-Request-Id", MdcUtils.convertIdNameToHeader("executionRequestId"));
        Assertions.assertEquals("X-Project-Id", MdcUtils.convertIdNameToHeader("projectId"));
    }

    /**
     * Test of putting null UUID value to MDC.
     */
    @Test
    public void testPutNullUUIDObjectSuccessful() {
        MdcUtils.put("key", (UUID) null);
        Assertions.assertNull(MDC.get("key"));
    }

}
