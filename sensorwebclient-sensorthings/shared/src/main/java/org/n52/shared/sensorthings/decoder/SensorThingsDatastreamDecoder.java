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
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsDatastream;
import org.n52.shared.serializable.pojos.sensorthings.UnitOfMeasruement;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;


public class SensorThingsDatastreamDecoder extends AbstractSensorThingsDecoder<List<SensorThingsDatastream>> {
	
	public SensorThingsDatastreamDecoder(IServiceAdapter adapter) {
		super(adapter);
	}
	
	@Override
	public List<SensorThingsDatastream> decode(OperationResult result) throws IOException, ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
		}
		return null;
	}

	@Override
	public List<SensorThingsDatastream> decode(JsonNode node) throws IOException, ExceptionReport, OXFException {
		List<SensorThingsDatastream> datastreams = new ArrayList<SensorThingsDatastream>();
		List<JsonNode> nodes = parseValue(node);
		if (nodes != null) {
			Set<Long> ids = new HashSet<Long>();
			for (JsonNode jsonNode : nodes) {
				jsonNode = checkForObject(jsonNode, DATASTREAM);
				long id = parseId(jsonNode);
				if (id != -1 && !ids.contains(id)) {
					SensorThingsDatastream datastream = parseDatastream(jsonNode);
					if (datastream != null) {
						datastreams.add(datastream);
					}
					ids.add(id);
				}
			}
		}
		if (hasNextLink(node)) {
			datastreams.addAll(getNext(parseNextLink(node)));
		}
		return datastreams;
	}
	
	private SensorThingsDatastream parseDatastream(JsonNode node) {
		if (node != null && !node.isMissingNode()) {
			SensorThingsDatastream datastream = new SensorThingsDatastream(parseId(node));
			datastream.setSelfLink(parseSelfLink(node));
			datastream.setThingNavigationLink(parseNavigationLink(node, THING));
			datastream.setSensorNavigationLink(parseNavigationLink(node, SENSOR));
			datastream.setObservedPropertyNavigationLink(parseNavigationLink(node, OBSERVED_PROPERTY));
			datastream.setObservationsNavigationLink(parseNavigationLink(node, OBSERVATIONS));
			datastream.setDescription(parseDescription(node));
			datastream.setUnitOfMeasurement(parseUnitOfMeasurement(node));
			datastream.setObservationType(parseObservationType(node));
			datastream.setObservedArea(parseObservedArea(node));
			datastream.setPhenomenonTime(parsePhenomenonTime(node));
			datastream.setResultTime(parseResultTime(node));
			return datastream;
		}
		return null;
	}

	private List<SensorThingsDatastream> getNext(String link) throws IOException, ExceptionReport, OXFException {
		return decode(getAdapter().doOperation(getOperation(link), new ParameterContainer()));
	}

	private Operation getOperation(String url) {
		return new Operation(DATASTREAMS, url, url);
	}

	private UnitOfMeasruement parseUnitOfMeasurement(JsonNode node) {
		UnitOfMeasruement unit = new UnitOfMeasruement();
		unit.setName(parseName(node));
		unit.setSymbol(parseSymbole(node));
		unit.setDefinition(parseDefinition(node));
		return unit;
	}

	private String parseSymbole(JsonNode node) {
		JsonNode path = node.path(SYMBOL);
		if (path != null && !path.isMissingNode()) {
			return path.asText();
		}
		return null;
	}

	private String parseObservationType(JsonNode node) {
		JsonNode path = node.path(OBSERVATION_TYPE);
		if (path != null && !path.isMissingNode()) {
			return path.asText();
		}
		return null;
	}

	private Geometry parseObservedArea(JsonNode node) {
		JsonNode path = node.path(OBSERVED_AREA);
		if (path != null && !path.isMissingNode()) {
			return GeoJSONDecoder.decode(path);
		}
		return null;
	}

}
