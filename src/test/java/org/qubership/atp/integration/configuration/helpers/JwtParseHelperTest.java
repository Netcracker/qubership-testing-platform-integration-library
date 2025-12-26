package org.qubership.atp.integration.configuration.helpers;

import static org.qubership.atp.integration.configuration.helpers.JwtParseHelper.BLANK_TOKEN_ERROR;
import static org.qubership.atp.integration.configuration.helpers.JwtParseHelper.PARSE_TOKEN_ERROR;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.integration.configuration.filters.AuditLoggingFilterTest;

public class JwtParseHelperTest {

    final JwtParseHelper jwtParseHelper = new JwtParseHelper();
    private final String ERROR_MESSAGE = "IllegalStateException should be thrown";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void isM2MtokenTest() {
        boolean result = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assert.assertFalse("The token should be not a M2M token", result);

        result = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assert.assertTrue("The token should be a M2M token", result);

        boolean[] flag = new boolean[1];
        IllegalStateException thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> flag[0] = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> flag[0] = jwtParseHelper.isM2Mtoken(StringUtils.EMPTY));
        Assert.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> flag[0] = jwtParseHelper.isM2Mtoken(AuditLoggingFilterTest.TEST_BASIC_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getSessionIdFromTokenTest() {
        UUID sessionId = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assert.assertEquals("User token: 'session_state' property of 'payload' object should be session UUID",
                UUID.fromString("8085b7d3-9472-470a-b914-d70071d2b072"), sessionId);

        sessionId = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assert.assertEquals("M2M token: 'session_state' property of 'payload' object should be session UUID",
                UUID.fromString("6288b3f8-2e02-42a1-8619-920cc596b6f4"), sessionId);

        UUID[] uuids = new UUID[1];
        IllegalStateException thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getSessionIdFromToken(StringUtils.EMPTY));
        Assert.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getSessionIdFromToken(AuditLoggingFilterTest.TEST_BASIC_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getUsernameFromTokenTest() {
        String username = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assert.assertEquals("User token: 'name' property of 'payload' object should be user name",
                "Example User", username);

        username = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assert.assertNull("M2M token: there should be no 'name' property in the 'payload' object", username);

        String[] names = new String[1];
        IllegalStateException thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> names[0] = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> names[0] = jwtParseHelper.getUsernameFromToken(StringUtils.EMPTY));
        Assert.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> names[0] = jwtParseHelper.getUsernameFromToken(AuditLoggingFilterTest.TEST_BASIC_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getUserIdFromTokenTest() {
        UUID userId = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assert.assertEquals("User token: 'sub' property of 'payload' object should be user UUID",
                UUID.fromString("c2344d70-3707-4418-a9c9-dbdb8beca796"), userId);

        userId = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assert.assertEquals("M2M token: 'sub' property of 'payload' object should be user UUID",
                UUID.fromString("2cabae38-420a-4c23-8c83-88b210e397cd"), userId);

        UUID[] uuids = new UUID[1];
        IllegalStateException thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromToken(StringUtils.EMPTY));
        Assert.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromToken(AuditLoggingFilterTest.TEST_BASIC_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }

    @Test
    public void getUserIdFromNonM2MTokenTest() {
        UUID userId = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.TEST_AUTH_HEADER);
        Assert.assertEquals("User token: 'sub' property of 'payload' object should be user UUID",
                UUID.fromString("c2344d70-3707-4418-a9c9-dbdb8beca796"), userId);

        userId = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.TEST_M2M_HEADER);
        Assert.assertNull("M2M token: userId should be null even if 'sub' property of 'payload' is present", userId);

        UUID[] uuids = new UUID[1];
        IllegalStateException thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.BROKEN_BEARER_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromNonM2MToken(StringUtils.EMPTY));
        Assert.assertEquals(BLANK_TOKEN_ERROR, thrown.getMessage());

        thrown = Assert.assertThrows(ERROR_MESSAGE,
                IllegalStateException.class,
                () -> uuids[0] = jwtParseHelper.getUserIdFromNonM2MToken(AuditLoggingFilterTest.TEST_BASIC_HEADER));
        Assert.assertEquals(PARSE_TOKEN_ERROR, thrown.getMessage());
    }
}