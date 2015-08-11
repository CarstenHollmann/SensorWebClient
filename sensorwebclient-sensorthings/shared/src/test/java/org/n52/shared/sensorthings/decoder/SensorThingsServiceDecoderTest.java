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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.OperationsMetadata;

import com.fasterxml.jackson.databind.JsonNode;

public class SensorThingsServiceDecoderTest extends AbstractSensorThingsDecoderTest {

	private JsonNode node;
	
	private SensorThingsServiceDecoder decoder;
	
    @Before
    public void setUp() {
    	node = getJsonFromFile("/st_main.json");
    	decoder = new SensorThingsServiceDecoder(null);
    }
	
	@Test
	public void test_parsing_service() throws IOException, ExceptionReport, OXFException {
		OperationsMetadata om = decoder.decode(node);
		assertThat(om.getOperations(), notNullValue(null));
		assertThat(om.getOperations().length, is(7));
	}
	
	
}
