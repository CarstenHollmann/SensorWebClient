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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsObservedProperty;

import com.fasterxml.jackson.databind.JsonNode;

public class SensorThingsObservedPropertyDecoder extends AbstractSensorThingsDecoder<List<SensorThingsObservedProperty>> {
	
	private String serviceUrl;
	
	public SensorThingsObservedPropertyDecoder(IServiceAdapter adapter, String serviceUrl) {
		super(adapter);
		this.serviceUrl = serviceUrl;
	}

	@Override
	public List<SensorThingsObservedProperty> decode(OperationResult result) throws IOException, ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
		}
		return null;
	}

	@Override
	public List<SensorThingsObservedProperty> decode(JsonNode node) throws IOException, ExceptionReport, OXFException {
		List<SensorThingsObservedProperty> obsProps = new ArrayList<SensorThingsObservedProperty>();
		Set<Long> ids = new HashSet<Long>();
		for (JsonNode jsonNode : getNodes(node)) {
			jsonNode = checkForObject(jsonNode, OBSERVED_PROPERTY);
			long id = parseId(jsonNode);
			if (id != -1 && !ids.contains(id)) {
				SensorThingsObservedProperty obsProp = new SensorThingsObservedProperty(id, serviceUrl);
				obsProp.setSelfLink(parseSelfLink(jsonNode));
				obsProp.setDatastreamsNavigationLink(parseNavigationLink(jsonNode, DATASTREAMS));
				obsProp.setName(parseName(jsonNode));
				obsProp.setDescription(parseDescription(jsonNode));
				obsProp.setDefinition(parseDefinition(jsonNode));
				obsProp.setLabel(obsProp.getName());
				obsProps.add(obsProp);
				ids.add(id);
			}
		}
		return obsProps;
	}

}
