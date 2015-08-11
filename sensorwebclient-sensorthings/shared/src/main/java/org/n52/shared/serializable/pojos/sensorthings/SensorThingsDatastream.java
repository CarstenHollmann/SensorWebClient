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

import org.n52.oxf.ows.capabilities.ITime;

import com.vividsolutions.jts.geom.Geometry;

public class SensorThingsDatastream implements SensorThingsObject, Serializable {
	
	private static final long serialVersionUID = 8084273903550304988L;

	private long id;
	
	private String selfLink;

	private String thingNavigationLink;

	private String sensorNavigationLink;
	
	private String observedPropertyNavigationLink;
	
	private String observationsNavigationLink;
	
	private String description;
	
	private UnitOfMeasruement unitOfMeasurement;
	
	private String observationType;
	
	private Geometry observedArea;
	
	private ITime phenomenonTime;
	
	private ITime resultTime;
	
	protected SensorThingsDatastream() {
		// for serialization
		super();
	}
	
	public SensorThingsDatastream(String parameterId) {
		this.id = Long.parseLong(parameterId);
	}
	
	public SensorThingsDatastream(long parameterId) {
		this.id = parameterId;
	}

	@Override
	public long getId() {
//		return Long.parseLong(getParameterId());
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
	 * @return the thingNavigationLink
	 */
	public String getThingNavigationLink() {
		return thingNavigationLink;
	}

	/**
	 * @param thingNavigationLink the thingNavigationLink to set
	 */
	public void setThingNavigationLink(String thingNavigationLink) {
		this.thingNavigationLink = thingNavigationLink;
	}

	/**
	 * @return the sensorNavigationLink
	 */
	public String getSensorNavigationLink() {
		return sensorNavigationLink;
	}

	/**
	 * @param sensorNavigationLink the sensorNavigationLink to set
	 */
	public void setSensorNavigationLink(String sensorNavigationLink) {
		this.sensorNavigationLink = sensorNavigationLink;
	}

	/**
	 * @return the observedPropertyNavigationLink
	 */
	public String getObservedPropertyNavigationLink() {
		return observedPropertyNavigationLink;
	}

	/**
	 * @param observedPropertyNavigationLink the observedPropertyNavigationLink to set
	 */
	public void setObservedPropertyNavigationLink(
			String observedPropertyNavigationLink) {
		this.observedPropertyNavigationLink = observedPropertyNavigationLink;
	}

	/**
	 * @return the observationsNavigationLink
	 */
	public String getObservationsNavigationLink() {
		return observationsNavigationLink;
	}

	/**
	 * @param observationsNavigationLink the observationsNavigationLink to set
	 */
	public void setObservationsNavigationLink(String observationsNavigationLink) {
		this.observationsNavigationLink = observationsNavigationLink;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the unitOfMeasurement
	 */
	public UnitOfMeasruement getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	/**
	 * @param unitOfMeasurement the unitOfMeasurement to set
	 */
	public void setUnitOfMeasurement(UnitOfMeasruement unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}

	/**
	 * @return the observationType
	 */
	public String getObservationType() {
		return observationType;
	}

	/**
	 * @param observationType the observationType to set
	 */
	public void setObservationType(String observationType) {
		this.observationType = observationType;
	}

	/**
	 * @return the observedArea
	 */
	public Geometry getObservedArea() {
		return observedArea;
	}

	/**
	 * @param observedArea the observedArea to set
	 */
	public void setObservedArea(Geometry observedArea) {
		this.observedArea = observedArea;
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

}
