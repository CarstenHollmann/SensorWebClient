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

import org.n52.shared.serializable.pojos.sos.Phenomenon;

public class SensorThingsObservedProperty extends Phenomenon implements SensorThingsObject, Serializable {

	private static final long serialVersionUID = 8443771705988877222L;

	private String selfLink;
	
	private String datastreamsNavigationLink;

	private String description;

	private String name;

	private String definition;
	
	protected SensorThingsObservedProperty() {
		super();
		// for serialization
	}

	public SensorThingsObservedProperty(String parameterId, String serviceUrl) {
		super(parameterId, serviceUrl);
	}
	
	public SensorThingsObservedProperty(long parameterId, String serviceUrl) {
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition
	 *            the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

}
