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
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.jwt.JwtHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtParseHelper {

    private static final String SESSION_STATE_KEY = "session_state";
    private static final String NAME_KEY = "name";
    private static final String USER_ID_KEY = "sub";
    private static final String CLIENT_ID_KEY = "clientId";

    /**
     * Detect if current auh token is M2M.
     */
    public boolean isM2Mtoken(String token) {
        String clientId = getValueFromTokenByKey(token, CLIENT_ID_KEY);

        return StringUtils.isNotBlank(clientId);
    }

    /**
     * Get session id attribute from JWT token.
     */
    public UUID getSessionIdFromToken(String token) {
        String sessionStateValue = getValueFromTokenByKey(token, SESSION_STATE_KEY);

        return UUID.fromString(sessionStateValue);
    }

    public String getUsernameFromToken(String token) {
        return getValueFromTokenByKey(token, NAME_KEY);
    }

    /**
     * Get user id attribute from JWT token.
     */
    public UUID getUserIdFromToken(String token) {
        String userIdValue = getValueFromTokenByKey(token, USER_ID_KEY);

        return UUID.fromString(userIdValue);
    }

    private String getValueFromTokenByKey(String token, String key) {
        if (StringUtils.isNotBlank(token)) {
            try {
                String[] splitToken = token.split(" ");
                if (splitToken.length < 2) {
                    return null;
                }
                token = splitToken[1];
                JsonParser parser = JsonParserFactory.getJsonParser();
                String claims = JwtHelper.decode(token).getClaims();
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
