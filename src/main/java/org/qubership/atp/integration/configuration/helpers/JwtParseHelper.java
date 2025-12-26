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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.jwt.JwtHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtParseHelper {

    /**
     * Token parse exception message.
     */
    public static final String PARSE_TOKEN_ERROR = "Failed to parse authorization token";

    /**
     * Blank token exception message.
     */
    public static final String BLANK_TOKEN_ERROR = "Blank authorization token error";

    /**
     * Name of property with session id value.
     */
    private static final String SESSION_STATE_KEY = "session_state";

    /**
     * Name of property with username value.
     */
    private static final String NAME_KEY = "name";

    /**
     * Name of property with user id value.
     */
    private static final String USER_ID_KEY = "sub";

    /**
     * Name of property with client id value.
     */
    private static final String CLIENT_ID_KEY = "clientId";

    /**
     * Detect if current auth token is M2M.
     *
     * @param token String token to parse
     * @return true if it's M2M token; otherwise false.
     */
    public boolean isM2Mtoken(final String token) {
        return StringUtils.isNotBlank(getValueFromTokenByKey(token, CLIENT_ID_KEY));
    }

    /**
     * Get session id attribute from JWT token.
     *
     * @param token String token to parse
     * @return UUID session id.
     */
    public UUID getSessionIdFromToken(final String token) {
        return UUID.fromString(Objects.requireNonNull(getValueFromTokenByKey(token, SESSION_STATE_KEY)));
    }

    /**
     * Get session id attribute from Token Data Map.
     *
     * @param tokenDataMap Map of Token Data
     * @return UUID session id.
     */
    public UUID getSessionIdFromTokenDataMap(final Map<String, ?> tokenDataMap) {
        Object sessionId = tokenDataMap.get(SESSION_STATE_KEY);
        String sessionIdKey = sessionId == null ? null : sessionId.toString();
        return UUID.fromString(Objects.requireNonNull(sessionIdKey));
    }

    /**
     * Get username attribute from JWT token.
     *
     * @param token String token to parse
     * @return String username.
     */
    public String getUsernameFromToken(final String token) {
        return getValueFromTokenByKey(token, NAME_KEY);
    }

    /**
     * Get username attribute from Token Data Map.
     *
     * @param tokenDataMap Map of Token Data
     * @return String username.
     */
    public String getUsernameFromTokenDataMap(final Map<String, ?> tokenDataMap) {
        Object username = tokenDataMap.get(NAME_KEY);
        return username == null ? null : username.toString();
    }

    /**
     * Get user id attribute from JWT token.
     *
     * @param token String token to parse
     * @return UUID user id.
     */
    public UUID getUserIdFromToken(final String token) {
        return UUID.fromString(Objects.requireNonNull(getValueFromTokenByKey(token, USER_ID_KEY)));
    }

    /**
     * Get user id attribute from Token Data Map.
     *
     * @param tokenDataMap Map of Token Data
     * @return UUID user id.
     */
    public UUID getUserIdFromTokenDataMap(final Map<String, ?> tokenDataMap) {
        Object userId = tokenDataMap.get(USER_ID_KEY);
        String userIdKey = userId == null ? null : userId.toString();
        return UUID.fromString(Objects.requireNonNull(userIdKey));
    }

    /**
     * Get user id attribute from non-M2M JWT token.
     *
     * @param token String token to parse
     * @return UUID user id, or null in case it's M2M token.
     */
    public UUID getUserIdFromNonM2MToken(final String token) {
        Map<String, ?> tokenDataMap = getTokenDataMapFromToken(token);
        if (tokenDataMap != null) {
            // Check if it's M2M token or not
            Object clientId = tokenDataMap.get(CLIENT_ID_KEY);
            String clientIdKey = clientId == null ? null : clientId.toString();
            if (StringUtils.isBlank(clientIdKey)) {
                // It's non-M2M token. Get userId from it
                Object userId = tokenDataMap.get(USER_ID_KEY);
                String userIdKey = userId == null ? null : userId.toString();
                return UUID.fromString(Objects.requireNonNull(userIdKey));
            }
            return null;
        }
        /*
            In fact, we shouldn't visit this point, due to IllegalStateException thrown earlier.
         */
        return null;
    }

    private String getValueFromTokenByKey(final String token, final String key) {
        Map<String, ?> tokenDataMap = getTokenDataMapFromToken(token);
        if (tokenDataMap != null && tokenDataMap.containsKey(key)) {
            return tokenDataMap.get(key).toString();
        }
        return null;
    }

    public Map<String, ?> getTokenDataMapFromToken(final String token) {
        if (StringUtils.isNotBlank(token)) {
            try {
                String[] splitToken = token.split(" ");
                if (splitToken.length < 2) {
                    return null;
                }
                JsonParser parser = JsonParserFactory.getJsonParser();
                return parser.parseMap(JwtHelper.decode(splitToken[1]).getClaims());
            } catch (Exception e) {
                log.error(PARSE_TOKEN_ERROR, e);
                throw new IllegalStateException(PARSE_TOKEN_ERROR);
            }
        } else {
            log.error(BLANK_TOKEN_ERROR);
            throw new IllegalStateException(BLANK_TOKEN_ERROR);
        }
    }
}
