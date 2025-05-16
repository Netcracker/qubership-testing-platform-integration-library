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

package org.qubership.atp.integration.configuration.model;

import java.util.Map;

import lombok.Data;

@Data
public class KafkaMailResponse {

    /**
     * Service name.
     */
    private String service;

    /**
     * Metadata map.
     */
    private Map<String, Object> metadata;

    /**
     * Response Status (Success or Error).
     */
    private KafkaMailResponseStatus status;

    /**
     * Response message.
     */
    private String message;

    /**
     * Stacktrace in case exception was faced.
     */
    private String stacktrace;
}

