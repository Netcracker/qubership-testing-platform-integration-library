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

package org.qubership.atp.integration.configuration.helpers;

import static org.qubership.atp.integration.configuration.helpers.JwtParseHelper.BLANK_TOKEN_ERROR;
import static org.qubership.atp.integration.configuration.helpers.JwtParseHelper.PARSE_TOKEN_ERROR;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.integration.configuration.filters.AuditLoggingFilterTest;

public class JwtParseHelperTest {

    final JwtParseHelper jwtParseHelper = new JwtParseHelper();
    private final String ERROR_MESSAGE = "IllegalStateException should be thrown";

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void isM2MtokenTest() {
        boolean result = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assertions.assertFalse(result, "The token should be not a M2M token");

        result = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assertions.assertTrue(result, "The token should be a M2M token");

        boolean[] flag = new boolean[1];
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> flag[0] = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> flag[0] = jwtParseHelper.isM2Mtoken(StringUtils.EMPTY),
                ERROR_MESSAGE);
        Assertions.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> flag[0] = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.TEST_BASIC_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getSessionIdFromTokenTest() {
        UUID sessionId = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assertions.assertEquals(UUID.fromString("8085b7d3-9472-470a-b914-d70071d2b072"),
                sessionId, "User token: 'session_state' property of 'payload' object should be session UUID");

        sessionId = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assertions.assertEquals(UUID.fromString("6288b3f8-2e02-42a1-8619-920cc596b6f4"),
                sessionId, "M2M token: 'session_state' property of 'payload' object should be session UUID");

        UUID[] uuids = new UUID[1];
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getSessionIdFromToken(StringUtils.EMPTY),
                ERROR_MESSAGE);
        Assertions.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.TEST_BASIC_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getUsernameFromTokenTest() {
        String username = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assertions.assertEquals("Example User",
                username, "User token: 'name' property of 'payload' object should be user name");

        username = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assertions.assertNull(username, "M2M token: there should be no 'name' property in the 'payload' object");

        String[] names = new String[1];
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> names[0] = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> names[0] = jwtParseHelper.getUsernameFromToken(StringUtils.EMPTY),
                ERROR_MESSAGE);
        Assertions.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> names[0] = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.TEST_BASIC_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getUserIdFromTokenTest() {
        UUID userId = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assertions.assertEquals(UUID.fromString("c2344d70-3707-4418-a9c9-dbdb8beca796"),
                userId, "User token: 'sub' property of 'payload' object should be user UUID");

        userId = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assertions.assertEquals(UUID.fromString("2cabae38-420a-4c23-8c83-88b210e397cd"),
                userId, "M2M token: 'sub' property of 'payload' object should be user UUID");

        UUID[] uuids = new UUID[1];
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromToken(StringUtils.EMPTY),
                ERROR_MESSAGE);
        Assertions.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.TEST_BASIC_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getUserIdFromNonM2MTokenTest() {
        UUID userId = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assertions.assertEquals(UUID.fromString("c2344d70-3707-4418-a9c9-dbdb8beca796"),
                userId, "User token: 'sub' property of 'payload' object should be user UUID");

        userId = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assertions.assertNull(userId, "M2M token: userId should be null even if 'sub' property of 'payload' is present");

        UUID[] uuids = new UUID[1];
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromNonM2MToken(StringUtils.EMPTY),
                ERROR_MESSAGE);
        Assertions.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.TEST_BASIC_HEADER),
                ERROR_MESSAGE);
        Assertions.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }
}