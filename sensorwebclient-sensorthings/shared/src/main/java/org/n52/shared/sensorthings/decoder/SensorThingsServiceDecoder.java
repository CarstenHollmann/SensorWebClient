/**
 * Copyright (C) 2012-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.shared.sensorthings.decoder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.ows.capabilities.OperationsMetadata;

import com.fasterxml.jackson.databind.JsonNode;

public class SensorThingsServiceDecoder extends AbstractSensorThingsDecoder<OperationsMetadata> {
	
	public SensorThingsServiceDecoder(IServiceAdapter adapter) {
		super(adapter);
	}

	@Override
	public OperationsMetadata decode(OperationResult result) throws IOException, ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
		}
		return null;
	}


	@Override
	public OperationsMetadata decode(JsonNode node) throws IOException, ExceptionReport, OXFException {
		Set<Operation> operations = new HashSet<Operation>();
		List<JsonNode> nodes = parseValue(node);
		if (nodes != null) {
			for (JsonNode jsonNode : nodes) {
				Operation operation = parseOperation(jsonNode);
				if (operation != null) {
					operations.add(operation);
				}
			}
		}
		return new OperationsMetadata(operations.toArray(new Operation[0]));
	}


	private Operation parseOperation(JsonNode node) {
		if (node != null && node.isObject()) {
			String url = parseUrl(node);
			return new Operation(parseName(node), url, url);
		}
		return null;
	}
	
	private String parseUrl(JsonNode node) {
		JsonNode path = node.path(URL);
		if (path != null) {
			return path.asText();
		}
		return null;
	}

}
