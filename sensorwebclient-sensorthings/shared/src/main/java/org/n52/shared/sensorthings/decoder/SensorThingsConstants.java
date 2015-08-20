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

public interface SensorThingsConstants {
	
	String URI_PATH = "uriPath";
	
	String PARAMETER = "parameter";
	
	String SERVICE = "Service";

	String VALUE = "value";
			
	String NAME = "name";
	
	String URL = "url";
	
	String ID = "id";
	
	String IOT = "iot";
	
	String AT_SIGN = "@";
	
	String DOT_SIGN = ".";
 	
	String AT_IOT_ID = AT_SIGN + IOT + DOT_SIGN + ID;
	
	String SELF_LINK = "selflink";
	
	String AT_IOT_SELF_LINK = AT_SIGN + IOT + DOT_SIGN + SELF_LINK;
	
	String SELF_LINKS = "selflinks";
	
	String NAVIGATION_LINK = "navigationLink";
	
	String AT_IOT_NAVIGATION_LINK = AT_SIGN + IOT + DOT_SIGN + NAVIGATION_LINK;
	
	String NEXT_LINK = "nextLink";
	
	String AT_IOT_NEXT_LINK = AT_SIGN + IOT + DOT_SIGN + NEXT_LINK;
	
	String VALUES = "values";
	
	String THINGS = "Things";
	
	String THING = "Thing";
	
	String LOCATIONS = "Locations";
	
	String HISTORICAL_LOCATIONS = "HistoricalLocations";
	
	String DATASTREAMS = "Datastreams";
	
	String DATASTREAM = "Datastream";
	
	String SENSORS = "Sensors";
	
	String SENSOR = "Sensor";
	
	String OBSERVATIONS = "Observations";
	
	String OBSERVATION = "Observation";
	
	String OBSERVED_PROPERTIES = "ObservedProperties";
	
	String OBSERVED_PROPERTY = "ObservedProperty";
	
	String FEATURES_OF_INTEREST = "FeaturesOfInterest";
	
	String FEATURE_OF_INTEREST = "FeatureOfInterest";
	
	String DESCRIPTION = "description";
	
	String DEFINITION = "definition";
	
	String ENCODING_TYPE = "encodingType";
	
	String LOCATION = "location";
	
	String PROPERTIES = "properties";
	
	String TIME = "time";
	
	String UNIT_OF_MEASRUEMENT = "unitOfMeasurement";
	
	String OBSERVATION_TYPE = "observationType";
	
	String OBSERVED_AREA = "observedArea";
	
	String PHENOMENON_TIME = "phenomenonTime";
	
	String RESULT_TIME = "resultTime";
	
	String METADATA = "meatadata";
	
	String RESULT = "result";
	
	String RESULT_QUALITY = "resultQuality";
	
	String VALID_TIME = "validTime";
	
	String FEATURE = "feature";
	
	String SYMBOL = "symbol";
	
	// Query options
	
	String DOLLAR_SIGN = "$";
			
	String FILTER = DOLLAR_SIGN + "filter";
	
	String COUNT = DOLLAR_SIGN + "count";
	
	String ORDER_BY = DOLLAR_SIGN + "orderby";
	
	String SKIP = DOLLAR_SIGN + "skip";
	
	String TOP = DOLLAR_SIGN + "top";
	
	String EXPAND = DOLLAR_SIGN + "expand";
	
	String SELECT = DOLLAR_SIGN + "select";
	
	String ENCODING_TYPE_GEO_JSON = "application/vnd.geo+json";
	
	String ENCODING_TYPE_PDF = "application/pdf";
	
	String ENCODING_TYPE_SENSORML_20 = "http://www.opengis.net/doc/IS/SensorML/2.0";
	
	// Comparison operators
	
	String GE = "ge";
	
	String LE = "le";
	
	String EQ = "eq";
	
	// Logical operators
	
	String AND = "and";
	
	String SPACE = " ";
	
	String INVERTED_COMMA = "'";
	
	String COMMA = ",";
	
	String QUESTIONMARK = "?";
	
	String AND_SIGN = "&";
	
	String EQUAL_SIGN = "=";
}
