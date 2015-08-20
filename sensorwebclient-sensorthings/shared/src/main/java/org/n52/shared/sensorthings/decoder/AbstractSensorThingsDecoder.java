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

import org.n52.io.crs.CRSUtils;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.ITime;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 
 *
 */
public abstract class AbstractSensorThingsDecoder<T> implements SensorThingsConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSensorThingsDecoder.class);
	
	private IServiceAdapter adapter;
	
	public abstract T decode(OperationResult result) throws IOException, ExceptionReport, OXFException;
	
	public abstract T decode(JsonNode node) throws IOException, ExceptionReport, OXFException;
	
	public AbstractSensorThingsDecoder(IServiceAdapter adapter) {
		this.adapter = adapter;
	}
	
	protected IServiceAdapter getAdapter() {
		return adapter;
	}
	
	protected JsonNode checkForObject(JsonNode jsonNode, String name) {
		if (jsonNode.isObject()) {
			JsonNode path = jsonNode.path(name);
			if (checkNode(path)) {
				return path;
			}
		}
		return jsonNode;
	}
	
	public Set<String> getIds(JsonNode node) {
		Set<String> ids = new HashSet<String>();
		if (node != null) {
			node = checkForValue(node);
			if (node.isArray()) {
				for (JsonNode n : node) {
					ids.add(Long.toString(parseId(n)));
				}
			} else {
				ids.add(Long.toString(parseId(node)));
			}
		}
		return ids;
	}
	
	public List<JsonNode> getNodes(JsonNode node) {
		List<JsonNode> nodes= new ArrayList<JsonNode>();
		if (node != null) {
			node = checkForValue(node);
			if (node.isArray()) {
				for (JsonNode n : node) {
					nodes.add(n);
				}
			} else {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	private JsonNode checkForValue(JsonNode node) {
		JsonNode path = node.path(VALUE);
		if (!path.isMissingNode()) {
			return path;
		}
		return node;
	}

	protected long parseId(JsonNode node) {
		JsonNode atIotIdPath = node.path(AT_IOT_ID);
		if (checkNode(atIotIdPath)) {
			return atIotIdPath.asLong();
		} else {
			JsonNode idPath = node.path(ID);
			if (checkNode(idPath)) {
				return idPath.asLong();
			}
		}
		return -1;
	}

	protected boolean checkNode(JsonNode node) {
		return node != null  && !node.isMissingNode();
	}

	protected String parseSelfLink(JsonNode node) {
		JsonNode atIotSelfLinkPath = node.path(AT_IOT_SELF_LINK);
		if (checkNode(atIotSelfLinkPath)) {
			return atIotSelfLinkPath.asText();
		} else {
			JsonNode selfLinkPath = node.path(SELF_LINK);
			if (checkNode(selfLinkPath)) {
				return selfLinkPath.asText();
			}
		}
		return null;
	}
	
	protected String parseDescription(JsonNode node) {
		JsonNode path = node.path(DESCRIPTION);
		if (checkNode(path)) {
			return path.asText();
		}
		return null;
	}
	
	protected String parseDefinition(JsonNode node) {
		JsonNode path = node.path(DEFINITION);
		if (checkNode(path)) {
			return path.asText();
		}
		return null;
	}
	
	protected String parseName(JsonNode node) {
		JsonNode path = node.path(NAME);
		if (checkNode(path)) {
			return path.asText();
		}
		return null;
	}
	
	protected String parseNavigationLink(JsonNode node, String name) {
		JsonNode atIotNavLinkPath = node.path(getNavigationLinkPath(name));
		if (checkNode(atIotNavLinkPath)) {
			return atIotNavLinkPath.asText();
		} else {
			JsonNode nameNavLinkPath = node.path(name);
			if (checkNode(nameNavLinkPath)) {
				JsonNode navLinkPath = node.path(name);
				if (checkNode(nameNavLinkPath)) {
					navLinkPath.asText();
				}
			}
		}
		return null;
	}
	
	protected String parseEncodingType(JsonNode node) {
		JsonNode path = node.path(getNavigationLinkPath(ENCODING_TYPE));
		if (checkNode(path)) {
			return path.asText();
		}
		return null;
	}

	private String getNavigationLinkPath(String name) {
		return name + AT_IOT_NAVIGATION_LINK;
	}
	
	protected Geometry createParsedPoint(JsonNode node, CRSUtils referenceHelper) {
		Geometry geometry = GeoJSONDecoder.decode(node);
		if (geometry != null) {
			try {
				return referenceHelper.transform(geometry,
						Integer.toString(geometry.getSRID()), null);
			} catch (FactoryException e) {
				LOGGER.warn("Could not create intern CRS.", e);
			} catch (TransformException e) {
				LOGGER.warn("Could not transform to intern CRS.", e);
			}
		}
		return geometry;
    }
	
	protected String parseNextLink(JsonNode node) {
		JsonNode atIotLinkPath = node.path(AT_IOT_NEXT_LINK);
		if (checkNode(atIotLinkPath)) {
			return atIotLinkPath.asText();
		} else {
			JsonNode linkPath = node.path(NEXT_LINK);
			if (checkNode(linkPath)) {
				return linkPath.asText();
			}
		}
		return null;
	}
	
	protected boolean hasNextLink(JsonNode node) {
		JsonNode atIotLinkPath = node.path(AT_IOT_NEXT_LINK);
		if (!checkNode(atIotLinkPath)) {
			JsonNode linkPath = node.path(NEXT_LINK);
			return checkNode(linkPath);
		}
		return true;
	}
	
	protected List<JsonNode> parseValue(JsonNode node) {
		JsonNode path = node.path(VALUE);
		if (checkNode(path)) {
			return getNodes(path);
		}
		return getNodes(node);
	}
	
	protected ITime parsePhenomenonTime(JsonNode node) {
		JsonNode path = node.path(PHENOMENON_TIME);
		if (checkNode(path)) {
			return parseTime(path.asText());
		}
		return null;
	}
	
	protected ITime parseResultTime(JsonNode node) {
		JsonNode path = node.path(RESULT_TIME);
		if (checkNode(path)) {
			return parseTime(path.asText());
		}
		return null;
	}

	private ITime parseTime(String timeString) {
		return TimeFactory.createTime(timeString);
	}
	
	protected String getBaseUriFromLink(String link) {
		if (checkNotNullOrEmptyString(link)) {
			return link.substring(0, link.indexOf(QUESTIONMARK));
		}
		return link;
	}

	protected ParameterContainer getParameterContainerFromLink(String link) throws OXFException {
		ParameterContainer container = new ParameterContainer();
		if (checkNotNullOrEmptyString(link)) {
			String[] uriParam = link.split("\\" + QUESTIONMARK);
			if (uriParam != null && uriParam.length == 2) {
				String parameterString = uriParam[1];
				if (checkNotNullOrEmptyString(parameterString)) {
					for (String parameter : parameterString.split(AND_SIGN)) {
						if (checkNotNullOrEmptyString(parameter)) {
							String[] split = parameter.split(EQUAL_SIGN);
							if (split != null && split.length == 2) {
								container.addParameterShell(split[0], split[1]);
							}
						}
					}
				}
			}
		}
		return container;
	}
	
	protected boolean checkNotNullOrEmptyString(String string) {
		return string != null && !string.isEmpty();
	}
	
}
