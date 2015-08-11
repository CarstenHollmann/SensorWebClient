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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsThings;

import com.fasterxml.jackson.databind.JsonNode;


public class SensorThingsThingsDecoder extends AbstractSensorThingsDecoder<List<SensorThingsThings>> {
	
	private String serviceUrl;

	public SensorThingsThingsDecoder(IServiceAdapter adapter, String serviceUrl) {
		super(adapter);
		this.serviceUrl = serviceUrl;
	}

	@Override
	public List<SensorThingsThings> decode(OperationResult result) throws IOException, ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
		}
		return null;
	}

	@Override
	public List<SensorThingsThings> decode(JsonNode node) throws IOException, ExceptionReport, OXFException {
		List<SensorThingsThings> things = new ArrayList<SensorThingsThings>();
		Set<Long> ids = new HashSet<Long>();
		for (JsonNode jsonNode : getNodes(node)) {
			jsonNode = checkForObject(jsonNode, THING);
			long id = parseId(jsonNode);
			if (id != -1 && !ids.contains(id)) {
				SensorThingsThings thing = new SensorThingsThings(id, serviceUrl);
				thing.setSelfLink(parseSelfLink(jsonNode));
				thing.setLocationsNavigationLink(parseNavigationLink(jsonNode, LOCATIONS));
				thing.setDatastreamsNavigationLink(parseNavigationLink(jsonNode, DATASTREAMS));
				thing.setHistoricalLocationsNavigationLink(parseNavigationLink(jsonNode, HISTORICAL_LOCATIONS));
				thing.setDescription(parseDescription(jsonNode));
				thing.setProperties(parseProperties(jsonNode));
				thing.setLabel(thing.getDescription());
				things.add(thing);
				ids.add(id);
			}
		}
		return things;
	}

	private Map<String, String> parseProperties(JsonNode node) {
		JsonNode path = node.path(PROPERTIES);
		if (!path.isMissingNode()) {
			Map<String, String> proeprties = new HashMap<String, String>();
			Iterator<String> it = path.fieldNames();
			while (it.hasNext()) {
				String name = (String) it.next();
				proeprties.put(name, path.findValue(name).asText());
			}
			return proeprties;
		}
		return null;
	}

}
