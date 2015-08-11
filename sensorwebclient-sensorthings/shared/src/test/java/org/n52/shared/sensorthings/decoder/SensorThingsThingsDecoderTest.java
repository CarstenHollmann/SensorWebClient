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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsThings;

import com.fasterxml.jackson.databind.JsonNode;

public class SensorThingsThingsDecoderTest extends AbstractSensorThingsDecoderTest {
	
	
	private JsonNode node;
	
	private SensorThingsThingsDecoder decoder;
	
    @Before
    public void setUp() {
    	node = getJsonFromFile("/st_thing.json");
    	decoder = new SensorThingsThingsDecoder(null, URL);
    }
	
	@Test
	public void test_parsing_id() {
		Set<String> ids = decoder.getIds(node);
		assertThat(ids.size(), is(1));
	}
	
	@Test
	public void test_parsing_node() throws IOException, ExceptionReport, OXFException {
		List<SensorThingsThings> set = decoder.decode(node);
		assertThat(set.size(), is(1));
	}

}
