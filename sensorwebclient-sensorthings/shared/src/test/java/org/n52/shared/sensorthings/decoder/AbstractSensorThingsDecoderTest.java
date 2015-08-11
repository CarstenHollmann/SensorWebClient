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
import java.io.InputStream;

import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class AbstractSensorThingsDecoderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSensorThingsDecoderTest.class);
	
	public static final String URL = "http://52north.org/test";
	
	public static final String VERSION = "v1.0";
	
	public static final String TITLE = "test";
	
	public static final SensorThingsMetadata metadata = new SensorThingsMetadata(URL, VERSION, TITLE); 

	protected JsonNode getJsonFromFile(String file) {
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(file);
			return JSONUtils.loadStream(is);
		} catch (Exception e) {
			LOGGER.error("Error while parsing InputStream!", e);
			// TODO: handle exception
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ioe) {
				LOGGER.error("Error while colsing InputStream!", ioe);
			}
		}
		return null;
	}
	
	
	protected InputStream getStreamFromFile(String file) {
		return getClass().getResourceAsStream(file);
	}
	
	protected OperationResult getOperationResult(String fileName) throws IOException {
		return new OperationResult(getStreamFromFile(fileName), new ParameterContainer(), "");
	}
	
}
