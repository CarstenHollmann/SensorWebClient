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

import org.n52.oxf.ows.capabilities.ITime;

public class SensorThingsObservation implements SensorThingsObject {
	
	private long id;
	
	private String selfLink;
	
	private String datastreamNavigationLink;

	private String featureOfInterestNavigationLink;
	
	private ITime phenomenonTime;
	
	private ITime resultTime;
	
	private Object result;

	public SensorThingsObservation(long id) {
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
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
	public String getDatastreamNavigationLink() {
		return datastreamNavigationLink;
	}

	/**
	 * @param datastreamNavigationLink the datastreamNavigationLink to set
	 */
	public void setDatastreamNavigationLink(String datastreamNavigationLink) {
		this.datastreamNavigationLink = datastreamNavigationLink;
	}

	/**
	 * @return the featureOfInterestNavigationLink
	 */
	public String getFeatureOfInterestNavigationLink() {
		return featureOfInterestNavigationLink;
	}

	/**
	 * @param featureOfInterestNavigationLink the featureOfInterestNavigationLink to set
	 */
	public void setFeatureOfInterestNavigationLink(
			String featureOfInterestNavigationLink) {
		this.featureOfInterestNavigationLink = featureOfInterestNavigationLink;
	}

	/**
	 * @return the phenomenonTime
	 */
	public ITime getPhenomenonTime() {
		return phenomenonTime;
	}

	/**
	 * @param phenomenonTime the phenomenonTime to set
	 */
	public void setPhenomenonTime(ITime phenomenonTime) {
		this.phenomenonTime = phenomenonTime;
	}

	/**
	 * @return the resultTime
	 */
	public ITime getResultTime() {
		return resultTime;
	}

	/**
	 * @param resultTime the resultTime to set
	 */
	public void setResultTime(ITime resultTime) {
		this.resultTime = resultTime;
	}

	/**
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}

}
