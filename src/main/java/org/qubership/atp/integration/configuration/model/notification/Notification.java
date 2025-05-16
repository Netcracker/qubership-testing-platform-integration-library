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

package org.qubership.atp.integration.configuration.model.notification;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
public class Notification {

    public enum Type {

        /**
         * No notification.
         */
        NONE("none"),

        /**
         * Info type (~level) of notification.
         */
        INFO("info"),

        /**
         * Warning type (~level) of notification.
         */
        WARNING("warn"),

        /**
         * Error type (~level) of notification.
         */
        ERROR("error"),

        /**
         * Success type of notification.
         */
        SUCCESS("success");

        /**
         * Type name.
         */
        private final String type;

        /**
         * Constructor.
         *
         * @param type String type name.
         */
        Type(final String type) {
            this.type = type;
        }

        /**
         * Make String representation.
         *
         * @return String representation.
         */
        @Override
        @JsonValue
        public String toString() {
            return this.type;
        }
    }

    /**
     * Notification message.
     */
    private String message;

    /**
     * Notification type.
     */
    private Type type;

    /**
     * UUID of the User to be notified.
     */
    private UUID userId;

    /**
     * Notification constructor.
     *
     * @param message notification message
     * @param type type of notification
     * @param userId the user id for which this notification is intended
     */
    public Notification(final String message, final Type type, final UUID userId) {
        this.message = message;
        this.type = type;
        this.userId = userId;
    }

    /**
     * Notification constructor.
     * Sets type of notification as 'none'.
     *
     * @param message notification message
     * @param userId the user id for which this notification is intended
     */
    public Notification(final String message, final UUID userId) {
        this.message = message;
        type = Type.NONE;
        this.userId = userId;
    }

}
