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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.integration.configuration.filters.AuditLoggingFilterTest;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

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

    @Test
    public void getTokenDataMapFromToken_shouldPreserveClaimTypes() {
        // Create JWT with different datatypes
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("stringClaim", "value")
                .claim("intClaim", 123)
                .claim("smallInt", 42)
                .claim("largeInt", 9999999999L) // Greater than Integer.MAX_VALUE
                .claim("double", 123.456)
                .claim("booleanClaim", true)
                .claim("listClaim", Arrays.asList("a", "b"))
                .claim("mapClaim", Map.of("key", "value"))
                .build();

        // signing is required
        //String jwtString = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet).serialize();

        String jwtString = new PlainJWT(claimsSet).serialize();

        Map<String, ?> result = jwtParseHelper.getTokenDataMapFromToken("Bearer " + jwtString);

        Assertions.assertInstanceOf(String.class, result.get("stringClaim"));
        Assertions.assertInstanceOf(Long.class, result.get("intClaim")); // integer numbers became Long
        Assertions.assertInstanceOf(Boolean.class, result.get("booleanClaim"));
        Assertions.assertInstanceOf(List.class, result.get("listClaim"));
        Assertions.assertInstanceOf(Map.class, result.get("mapClaim"));

        // Both int numbers should be Long
        Assertions.assertInstanceOf(Long.class, result.get("smallInt"));
        Assertions.assertInstanceOf(Long.class, result.get("largeInt"));
        Assertions.assertEquals(42L, result.get("smallInt"));
        Assertions.assertEquals(9999999999L, result.get("largeInt"));

        // Floating number should be Double
        Assertions.assertInstanceOf(Double.class, result.get("double"));
        Assertions.assertEquals(123.456, (Double) result.get("double"), 0.001);
    }

    @Test
    public void getTokenDataMapFromToken_shouldHandleNullClaims() {
        // JWTClaimsSet.Builder doesn't allow to set null directly,
        // so, check that missing claim is null if got from Map.
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("existing", "value")
                .build();

        String jwtString = new PlainJWT(claimsSet).serialize();
        Map<String, ?> result = jwtParseHelper.getTokenDataMapFromToken("Bearer " + jwtString);

        Assertions.assertNotNull(result.get("existing"));
        Assertions.assertNull(result.get("nonExisting"));
    }

    @Test
    public void getTokenDataMapFromToken_shouldReturnExpectedStructure() {
        // Test user token
        Map<String, ?> userTokenMap = jwtParseHelper.getTokenDataMapFromToken(
                AuditLoggingFilterTest.TEST_AUTH_HEADER);

        Assertions.assertNotNull(userTokenMap);
        Assertions.assertEquals("8085b7d3-9472-470a-b914-d70071d2b072", userTokenMap.get("session_state"));
        Assertions.assertEquals("Example User", userTokenMap.get("name"));
        Assertions.assertEquals("c2344d70-3707-4418-a9c9-dbdb8beca796", userTokenMap.get("sub"));

        // Verify no extra unexpected keys? Not necessary, just check critical ones
        Assertions.assertTrue(userTokenMap.containsKey("exp"), "Should have expiration claim");
        Assertions.assertTrue(userTokenMap.containsKey("iat"), "Should have issued at claim");

        // Test M2M token
        Map<String, ?> m2mTokenMap = jwtParseHelper.getTokenDataMapFromToken(
                AuditLoggingFilterTest.TEST_M2M_HEADER);

        Assertions.assertNotNull(m2mTokenMap);
        Assertions.assertEquals("6288b3f8-2e02-42a1-8619-920cc596b6f4", m2mTokenMap.get("session_state"));
        Assertions.assertEquals("2cabae38-420a-4c23-8c83-88b210e397cd", m2mTokenMap.get("sub"));
        Assertions.assertNotNull(m2mTokenMap.get("clientId"), "M2M token should have clientId");
        Assertions.assertNull(m2mTokenMap.get("name"), "M2M token should not have name");
    }

    @Test
    public void numericClaimsShouldBeLongs() {
        Map<String, ?> tokenData = jwtParseHelper.getTokenDataMapFromToken(
                AuditLoggingFilterTest.TEST_AUTH_HEADER);

        Object exp = tokenData.get("exp");
        Assertions.assertInstanceOf(Long.class, exp,
                "exp claim should be Long, got: " + exp.getClass());

        Object iat = tokenData.get("iat");
        Assertions.assertInstanceOf(Long.class, iat,
                "iat claim should be Long, got: " + iat.getClass());

        Object nbf = tokenData.get("nbf");
        Assertions.assertInstanceOf(Long.class, nbf,
                "nbf claim should be Long, got: " + nbf.getClass());
        Assertions.assertEquals(0L, nbf, "nbf should be 0 in the test auth header");
    }

    @Test
    public void parsePerformanceShouldBeAcceptable() {
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            jwtParseHelper.getTokenDataMapFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        }
        long duration = System.nanoTime() - start;

        long avgMicros = duration / 1000 / 1000;
        System.out.println("Average parse time: " + avgMicros + " μs");

        Assertions.assertTrue(avgMicros < 1000, "Parsing should take <1ms on average");
    }
}