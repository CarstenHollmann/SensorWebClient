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
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsObservation;

import com.fasterxml.jackson.databind.JsonNode;


public class SensorThingsObservationDecoder extends AbstractSensorThingsDecoder<List<SensorThingsObservation>> {

	public SensorThingsObservationDecoder(IServiceAdapter adapter) {
		super(adapter);
	}

	@Override
	public List<SensorThingsObservation> decode(OperationResult result) throws IOException,
			ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
		}
		return null;
	}

	@Override
	public List<SensorThingsObservation> decode(JsonNode node) throws IOException, ExceptionReport,
			OXFException {
		return decode(node, null);
	}

	public List<SensorThingsObservation> decode(OperationResult result, DesignOptions options) throws IOException,
	ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()), options);
		}
		return null;
	}
	
	public List<SensorThingsObservation> decode(JsonNode node, DesignOptions options) throws IOException, ExceptionReport,
			OXFException {
		List<SensorThingsObservation> observations = new ArrayList<SensorThingsObservation>();
		List<JsonNode> nodes = parseValue(node);
		if (nodes != null) {
			Set<Long> ids = new HashSet<Long>();
			for (JsonNode jsonNode : nodes) {
				jsonNode = checkForObject(jsonNode, OBSERVATION);
				long id = parseId(jsonNode);
				if (id != -1 && !ids.contains(id)) {
					SensorThingsObservation observation = parseObservation(jsonNode);
					if (observation != null) {
							observations.add(observation);
							ids.add(id);
					}
				}
			}
		}
		if (!checkForTimeParam(options) && hasNextLink(node)) {
			observations.addAll(getNext(parseNextLink(node), options));
		}
		return observations;
	}


	private boolean checkForTimeParam(DesignOptions options) {
		if (options != null) {
			return options.getTimeParam() != null;
		}
		return false;
	}

	private SensorThingsObservation parseObservation(JsonNode node) {
		SensorThingsObservation observation = new SensorThingsObservation(parseId(node));
		observation.setSelfLink(parseSelfLink(node));
		observation.setDatastreamNavigationLink(parseNavigationLink(node, DATASTREAM));
		observation.setFeatureOfInterestNavigationLink(parseNavigationLink(node, FEATURE_OF_INTEREST));
		observation.setPhenomenonTime(parsePhenomenonTime(node));
		observation.setResultTime(parseResultTime(node));
		observation.setResult(parseResult(node));
		return observation;
	}

	private Object parseResult(JsonNode node) {
		JsonNode path = node.path(RESULT);
		if (checkNode(path)) {
			if (path.isDouble()) {
				return path.doubleValue();
			} else if (path.isBigDecimal()) {
				return path.asDouble();
			} else if (path.isFloat()) {
				return path.asDouble();
			} else if (path.isBoolean()) {
				return path.asDouble();
			}  else if (path.isInt()) {
				return path.asDouble();
			}  else if (path.isLong()) {
				return path.asDouble();
			} else if (path.isTextual()) {
				return path.asDouble();
			}
		}
		return Double.NaN;
	}

	private List<SensorThingsObservation> getNext(String link, DesignOptions options) throws IOException, ExceptionReport, OXFException {
		String baseURI = getBaseUriFromLink(link);
		ParameterContainer container = getParameterContainerFromLink(link);
		return decode(getAdapter().doOperation(getOperation(baseURI), container), options);
	}

	private Operation getOperation(String url) {
		return new Operation(DATASTREAMS, url, url);
	}
}
