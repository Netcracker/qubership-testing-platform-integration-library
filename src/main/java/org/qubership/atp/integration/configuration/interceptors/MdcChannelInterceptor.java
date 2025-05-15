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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.integration.configuration.helpers.JwtParseHelper;
import org.qubership.atp.integration.configuration.helpers.StompHelper;
import org.qubership.atp.integration.configuration.mdc.MdcField;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MdcChannelInterceptor implements ChannelInterceptor {

    /**
     * StompHelper bean.
     */
    private final StompHelper bearerTokenStompHelper;

    /**
     * JwtParseHelper bean.
     */
    private final JwtParseHelper jwtParseHelper;

    /**
     * List of String business IDs.
     */
    private final List<String> businessIds;

    /**
     * Create and configure message handler.
     *
     * @param bearerTokenStompHelper StompHelper bean
     * @param parseHelper JwtParseHelper bean
     * @param businessIdsString String with List of business IDs separated by comma.
     */
    public MdcChannelInterceptor(final StompHelper bearerTokenStompHelper,
                                 final JwtParseHelper parseHelper,
                                 final String businessIdsString) {
        this.bearerTokenStompHelper = bearerTokenStompHelper;
        this.jwtParseHelper = parseHelper;
        this.businessIds = MdcUtils.convertIdNamesToList(businessIdsString);
    }

    /**
     * Pre-Send Message Handler.
     *
     * @param message Message to be processed
     * @param channel MessageChannel to send message to
     * @return message processed.
     */
    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        try {
            MDC.clear();
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null) {
                String token = this.bearerTokenStompHelper.extractBearerToken(accessor);
                if (token != null && StringUtils.startsWithIgnoreCase(token, "Bearer ")) {
                    try {
                        if (!jwtParseHelper.isM2Mtoken(token)) {
                            MdcUtils.put(MdcField.USER_ID.toString(), jwtParseHelper.getUserIdFromToken(token));
                        }
                    } catch (Exception e) {
                        log.error("Error while getting userId from token", e);
                    }
                }
                processHeaders(accessor);
            }
        } catch (Exception e) {
            log.error("Error occurred while web socket message pre-processing", e);
        }
        return message;
    }

    private void processHeaders(final StompHeaderAccessor accessor) {
        if (!CollectionUtils.isEmpty(businessIds)) {
            businessIds.forEach(idName -> {
                String header = accessor.getFirstNativeHeader(MdcUtils.convertIdNameToHeader(idName));
                if (header != null) {
                    MdcUtils.put(idName.trim(), header);
                }
            });
        }
    }
}
