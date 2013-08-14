package org.n52.api.v1.srv;

import static org.n52.server.mgmt.ConfigurationContext.getSOSMetadatas;

import java.util.ArrayList;
import java.util.List;

import org.n52.api.v1.io.ProcedureConverter;
import org.n52.io.v1.data.ProcedureOutput;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;
import org.n52.web.v1.srv.ParameterService;

public class ProceduresOutputAdapter implements ParameterService<ProcedureOutput> {

	@Override
	public ProcedureOutput[] getExpandedParameters(int offset, int size) {
		List<ProcedureOutput> allProcedures = new ArrayList<ProcedureOutput>();
		for (SOSMetadata metadata : getSOSMetadatas()) {
		    ProcedureConverter converter = new ProcedureConverter(metadata);
			TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
			allProcedures.addAll(converter.convertExpanded(lookup.getProceduresAsArray()));
		}
		return allProcedures.toArray(new ProcedureOutput[0]);
	}
	
	@Override
    public ProcedureOutput[] getCondensedParameters(int offset, int size) {
        List<ProcedureOutput> allProcedures = new ArrayList<ProcedureOutput>();
        for (SOSMetadata metadata : getSOSMetadatas()) {
            ProcedureConverter converter = new ProcedureConverter(metadata);
            TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
            allProcedures.addAll(converter.convertCondensed(lookup.getProceduresAsArray()));
        }
        return allProcedures.toArray(new ProcedureOutput[0]);
    }

	@Override
	public ProcedureOutput getParameter(String procedureId) {
		for (SOSMetadata metadata : getSOSMetadatas()) {
			TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
			if (lookup.containsProcedure(procedureId)) {
			    ProcedureConverter converter = new ProcedureConverter(metadata);
				return converter.convertExpanded(lookup.getProcedure(procedureId));
			}
		}
		return null;
	}


}