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
     * Get username attribute from JWT token.
     *
     * @param token String token to parse
     * @return String username.
     */
    public String getUsernameFromToken(final String token) {
        return getValueFromTokenByKey(token, NAME_KEY);
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

    private String getValueFromTokenByKey(final String token, final String key) {
        if (StringUtils.isNotBlank(token)) {
            try {
                String[] splitToken = token.split(" ");
                if (splitToken.length < 2) {
                    return null;
                }
                JsonParser parser = JsonParserFactory.getJsonParser();
                String claims = JwtHelper.decode(splitToken[1]).getClaims();
                Map<String, ?> tokenDataMap = parser.parseMap(claims);

                if (tokenDataMap.containsKey(key)) {
                    return tokenDataMap.get(key).toString();
                }
                return null;
            } catch (Exception e) {
                log.error("Cannot parse token with error: ", e);
                throw new IllegalStateException("Failed to parse authorization token");
            }
        } else {
            log.error("Blank authorization token error");
            throw new IllegalStateException("Blank authorization token error");
        }
    }
}
