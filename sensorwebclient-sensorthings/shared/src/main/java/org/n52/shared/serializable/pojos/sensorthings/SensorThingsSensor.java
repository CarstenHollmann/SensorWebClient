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
package org.n52.shared.serializable.pojos.sensorthings;

import java.io.Serializable;

import org.n52.shared.serializable.pojos.sos.Procedure;

public class SensorThingsSensor extends Procedure implements SensorThingsObject, Serializable {

	private static final long serialVersionUID = 860591777851020041L;

private String selfLink;
	
	private String datastreamsNavigationLink;

	private String description;

	private String encodingType;
	
	private String metadata;
	
	protected SensorThingsSensor() {
		// for serialization
		super();
	}

	public SensorThingsSensor(String parameterId, String serviceUrl) {
		super(parameterId, serviceUrl);
	}
	
	public SensorThingsSensor(long parameterId, String serviceUrl) {
		super(Long.toString(parameterId), serviceUrl);
	}

	@Override
	public long getId() {
		return Long.parseLong(getParameterId());
	}

	@Override
	public String getSelfLink() {
		return selfLink;
	}

	@Override
	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}
	
	/**
	 * @return the datastreamNavigationLink
	 */
	public String getDatastreamsNavigationLink() {
		return datastreamsNavigationLink;
	}

	/**
	 * @param datastreamNavigationLink
	 *            the datastreamNavigationLink to set
	 */
	public void setDatastreamsNavigationLink(String datastreamsNavigationLink) {
		this.datastreamsNavigationLink = datastreamsNavigationLink;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the encodingType
	 */
	public String getEncodingType() {
		return encodingType;
	}

	/**
	 * @param encodingType the encodingType to set
	 */
	public void setEncodingType(String encodingType) {
		this.encodingType = encodingType;
	}

	/**
	 * @return the metadata
	 */
	public String getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

}
