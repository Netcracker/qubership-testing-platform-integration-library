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

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AtpRoute {

    /**
     * Service id to use in registry.
     */
    private String serviceId;

    /**
     * Port number.
     */
    private int port;

    /**
     * Prefix path to route.
     */
    private String path;

    /**
     * Is the route public or not? (Should it be registered in public gateway or not?)
     */
    private boolean isPublic;

    /**
     * Is the route internal or not? (Should it be registered in internal gateway or not?)
     */
    private boolean isInternal;

    /**
     * Context path.
     */
    private String contextPath;

    /**
     * URL of the route.
     */
    private String url;

}
