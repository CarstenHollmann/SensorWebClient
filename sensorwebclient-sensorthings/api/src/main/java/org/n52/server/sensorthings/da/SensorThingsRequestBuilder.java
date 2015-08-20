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
package org.n52.server.sensorthings.da;

import static java.lang.String.format;

import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.adapter.ParameterShell;
import org.n52.oxf.ows.capabilities.IValueDomain;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.request.MultiValueRequestParameters;
import org.n52.oxf.request.RequestParameters;
import org.n52.oxf.valueDomains.IntegerDiscreteValueDomain;
import org.n52.oxf.valueDomains.StringValueDomain;
import org.n52.shared.sensorthings.decoder.SensorThingsConstants;

public class SensorThingsRequestBuilder implements SensorThingsConstants {
	
	public RequestParameters buildRequest(Operation operation, ParameterContainer parameters) throws OXFException {
		if (operation.getName().equals(THINGS)) {
            return buildThingsRequest(parameters);
        }
        else if (operation.getName().equals(LOCATIONS)) {
            return buildLocationsRequest(parameters);
        }
        else if (operation.getName().equals(DATASTREAMS)) {
            return buildDatastreamsRequest(parameters);
        }
        else if (operation.getName().equals(HISTORICAL_LOCATIONS)) {
            return buildHistoricalLocationsRequest(parameters);
        }
        else if (operation.getName().equals(SENSORS)) {
            return buildSensorsRequest(parameters);
        }
        else if (operation.getName().equals(OBSERVATIONS)) {
            return buildObservationsRequest(parameters);
        }
        else if (operation.getName().equals(OBSERVED_PROPERTIES)) {
            return buildObservedPropertiesRequest(parameters);
        }
        else if (operation.getName().equals(FEATURES_OF_INTEREST)) {
            return buildFeatureOfInterestRequest(parameters);
        } 
        else if (operation.getName().equals(SERVICE)) {
            return buildServiceRequest(parameters);
        }
        else {
            throw new OXFException(format("Operation '%s' not supported.", operation.getName()));
        }
	}

	private RequestParameters buildThingsRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildLocationsRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildDatastreamsRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}
	
	private RequestParameters buildHistoricalLocationsRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildSensorsRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildObservationsRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildObservedPropertiesRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildFeatureOfInterestRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters buildServiceRequest(ParameterContainer parameters) {
		return getRequestParameter(parameters);
	}

	private RequestParameters getRequestParameter(ParameterContainer parameters) {
		MultiValueRequestParameters requestParameters = new MultiValueRequestParameters();
		for (ParameterShell parameterShell : parameters.getParameterShells()) {
			IValueDomain valueDomain = parameterShell.getParameter().getValueDomain();
			if (valueDomain instanceof IntegerDiscreteValueDomain) {
				for (Integer i : parameterShell.getSpecifiedTypedValueArray(Integer[].class)) {
					requestParameters.addParameterValue(parameterShell.getParameter().getCommonName(), i.toString());
				}
			} else if (valueDomain instanceof StringValueDomain) {
				for (String s: parameterShell.getSpecifiedTypedValueArray(String[].class)) {
					requestParameters.addParameterValue(parameterShell.getParameter().getCommonName(), s);
				}
			} else {
				for (Object o : parameterShell.getSpecifiedTypedValueArray(Object[].class)) {
					requestParameters.addParameterValue(parameterShell.getParameter().getCommonName(), o.toString());
				}
			}
			
		}
		return requestParameters;
	}

	public String buildRequestUrl(Operation operation, ParameterContainer parameters) throws OXFException {
		if (operation.getName().equals(THINGS)) {
            return buildThingsRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(LOCATIONS)) {
            return buildLocationsRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(DATASTREAMS)) {
            return buildDatastreamsRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(LOCATIONS)) {
            return buildHistoricalLocationsRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(SENSORS)) {
            return buildSensorsRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(OBSERVATIONS)) {
            return buildObservationsRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(OBSERVED_PROPERTIES)) {
            return buildObservedPropertiesRequestUrl(operation, parameters);
        }
        else if (operation.getName().equals(FEATURES_OF_INTEREST)) {
            return buildFeatureOfInterestRequestUrl(operation, parameters);
        } else if (operation.getName().equals(SERVICE)) {
            return buildServiceUrl(operation, parameters);
        }
        else {
            throw new OXFException(format("Operation '%s' not supported.", operation.getName()));
        }
	}

	private String buildThingsRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}

	private String buildLocationsRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}

	private String buildDatastreamsRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}
	
	private String buildHistoricalLocationsRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}

	private String buildSensorsRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}

	private String buildObservationsRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}

	private String buildObservedPropertiesRequestUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = getUri(operation);
		baseUri = addId(baseUri, parameters);
		baseUri = addUriPath(baseUri, parameters);
		return baseUri;
	}

	private String buildFeatureOfInterestRequestUrl(Operation operation,ParameterContainer parameters) {
		String baseUri = getUri(operation);
		
		return baseUri;
	}
	
	private String buildServiceUrl(Operation operation, ParameterContainer parameters) {
		String baseUri = null;
        if (operation.getDcps()[0].getHTTPGetRequestMethods().size() > 0) {
        	baseUri = operation.getDcps()[0].getHTTPGetRequestMethods().get(0).getOnlineResource().getHref().trim();
        }
        
        return baseUri;
	}

	private String getUri(Operation operation) {
		String uri = null;
        if (operation.getDcps()[0].getHTTPGetRequestMethods().size() > 0) {
            uri = operation.getDcps()[0].getHTTPGetRequestMethods().get(0).getOnlineResource().getHref().trim();
        }
        if (!uri.contains(operation.getName())) {
        	uri = uri + "/" + operation.getName();
        }
        return uri.trim();
	}

	private String addId(String baseUri, ParameterContainer parameters) {
		StringBuilder builder = new StringBuilder(baseUri);
		if (parameters.containsParameterShellWithCommonName(ID)){
			ParameterShell shell = parameters.getParameterShellWithCommonName(ID);
			parameters.removeParameterShell(shell);
			builder.append("(");
			builder.append(shell.getSpecifiedValue());
			builder.append(")");
		}
		return builder.toString();
	}
	
	
	private String addUriPath(String baseUri, ParameterContainer parameters) {
		StringBuilder builder = new StringBuilder(baseUri);
		if (parameters.containsParameterShellWithCommonName(URI_PATH)){
			ParameterShell shell = parameters.getParameterShellWithCommonName(URI_PATH);
			parameters.removeParameterShell(shell);
			if (!shell.getSpecifiedValue().toString().startsWith("/")) {
				builder.append("/");
			}
			builder.append(shell.getSpecifiedValue());
		}
		return builder.toString();
	}

}
