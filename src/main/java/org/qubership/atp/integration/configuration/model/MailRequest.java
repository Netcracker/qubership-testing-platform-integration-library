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
public class MailRequest {

    /**
     * Service name.
     */
    private String service;

    /**
     * Metadata map.
     */
    private Map<String, Object> metadata;

    /**
     * Email address of sender.
     */
    private String from;

    /**
     * Email address(-es) of receiver(s).
     */
    private String to;

    /**
     * Email address(-es) of 'CC'-receiver(s).
     */
    private String cc;

    /**
     * Email subject.
     */
    private String subject;

    /**
     * Email body.
     */
    private String content;
}
