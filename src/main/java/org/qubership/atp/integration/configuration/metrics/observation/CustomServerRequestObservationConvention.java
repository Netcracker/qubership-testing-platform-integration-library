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
