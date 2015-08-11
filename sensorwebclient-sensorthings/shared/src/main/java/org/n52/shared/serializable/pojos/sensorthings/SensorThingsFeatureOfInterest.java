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

import org.n52.shared.serializable.pojos.sos.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class SensorThingsFeatureOfInterest extends Feature implements SensorThingsObject, Serializable {
	
	private static final long serialVersionUID = -7140461635706904040L;
	
	private String selfLink;
	
	private String observationsNavigationLink;

	private String description;
	
	private String encodingType;
	
	private Geometry feature;
	
	protected SensorThingsFeatureOfInterest() {
		// for serialization
		super();
	}
	
	public SensorThingsFeatureOfInterest(String parameterId, String serviceUrl) {
		super(parameterId, serviceUrl);
	}
	
	public SensorThingsFeatureOfInterest(long parameterId, String serviceUrl) {
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
	 * @return the feature
	 */
	public Geometry getFeature() {
		return feature;
	}

	/**
	 * @param feature the feature to set
	 */
	public void setFeature(Geometry feature) {
		this.feature = feature;
	}

	/**
	 * If the stored geometry is not a {@link Point}, the interior point is returned.
	 * 
	 * @return {@link Point}
	 */
	public Point getPointLocation() {
		if (getFeature() != null) {
			if (getFeature() instanceof Point) {
				return (Point) getFeature();
			}
			return getFeature().getInteriorPoint();
		}
		return null;
	}

}
