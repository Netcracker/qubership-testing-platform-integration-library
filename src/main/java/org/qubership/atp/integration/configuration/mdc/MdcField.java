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

package org.qubership.atp.integration.configuration.mdc;

public enum MdcField {

    /**
     * Name of MDC tag for project id.
     */
    PROJECT_ID("projectId"),

    /**
     * Name of MDC tag for user id.
     */
    USER_ID("userId");

    private final String name;

    /**
     * Constructor.
     *
     * @param type String name.
     */
    MdcField(final String type) {
        this.name = type;
    }

    /**
     * Make String representation.
     *
     * @return String representation.
     */
    @Override
    public String toString() {
        return this.name;
    }
}
