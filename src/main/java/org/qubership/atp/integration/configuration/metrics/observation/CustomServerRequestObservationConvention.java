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

package org.qubership.atp.integration.configuration.metrics.observation;

import org.qubership.atp.integration.configuration.mdc.MdcField;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.slf4j.MDC;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

public class CustomServerRequestObservationConvention extends DefaultServerRequestObservationConvention {
    public static final String UNKNOWN_PROJECT = "unknown";

    @Override
    public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
        return super.getLowCardinalityKeyValues(context).and(addProjectIdTag(context));
    }

    private KeyValue addProjectIdTag(ServerRequestObservationContext context) {
        String projectId = MdcUtils.getHeaderFromRequest(context.getCarrier(),
                MdcUtils.convertIdNameToHeader(MdcField.PROJECT_ID.toString()));
        projectId = projectId == null ? MDC.get(MdcField.PROJECT_ID.toString()) : projectId;
        return KeyValue.of(MdcField.PROJECT_ID.toString(), projectId == null ? UNKNOWN_PROJECT : projectId);
    }
}
