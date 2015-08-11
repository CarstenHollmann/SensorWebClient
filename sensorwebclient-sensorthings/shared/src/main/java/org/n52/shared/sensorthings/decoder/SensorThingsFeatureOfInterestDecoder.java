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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.io.crs.CRSUtils;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.shared.Metadata;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsFeatureOfInterest;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsMetadata;
import org.n52.shared.serializable.pojos.sos.Station;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;

public class SensorThingsFeatureOfInterestDecoder extends AbstractSensorThingsDecoder<List<SensorThingsFeatureOfInterest>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SensorThingsFeatureOfInterestDecoder.class);
	
	private OperationResult result;
	
	private SensorThingsMetadata metadata;

	private CRSUtils referenceHelper = CRSUtils.createEpsgStrictAxisOrder();
	
	public SensorThingsFeatureOfInterestDecoder(IServiceAdapter adapter, Metadata metadata) {
		super(adapter);
		if (result == null) {
            LOGGER.error("Get no result for GetFeatureOfInterest!");
        }
		this.metadata = (SensorThingsMetadata)metadata;
		if(metadata.isForceXYAxisOrder()) {
			referenceHelper = CRSUtils.createEpsgForcedXYAxisOrder();
		}
	}
	
	@Override
	public List<SensorThingsFeatureOfInterest> decode(OperationResult result) throws IOException, ExceptionReport, OXFException {
		if (result != null) {
			return decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
		}
		return null;
	}

	@Override
	public List<SensorThingsFeatureOfInterest> decode(JsonNode node) throws IOException, ExceptionReport, OXFException {
		List<SensorThingsFeatureOfInterest> features = new ArrayList<SensorThingsFeatureOfInterest>();
		Set<Long> ids = new HashSet<Long>();
		for (JsonNode jsonNode : getNodes(node)) {
			jsonNode = checkForObject(jsonNode, FEATURE_OF_INTEREST);
			long id = parseId(jsonNode);
			if (id != -1 && !ids.contains(id)) {
				SensorThingsFeatureOfInterest feature = new SensorThingsFeatureOfInterest(id, metadata.getServiceUrl());
				feature.setSelfLink(parseSelfLink(jsonNode));
				feature.setObservationsNavigationLink(parseNavigationLink(jsonNode, OBSERVATIONS));
				feature.setDescription(parseDescription(jsonNode));
				feature.setEncodingType(parseEncodingType(jsonNode));
				feature.setFeature(parseFeature(jsonNode));
				
				feature.setLabel(feature.getDescription());
				features.add(feature);
				ids.add(id);
			}
		}
		return features;
	}

	public List<Station> createStations(Collection<SensorThingsFeatureOfInterest> features) {
		List<Station> stations = new ArrayList<Station>();
        TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
        for (SensorThingsFeatureOfInterest feature : features) {
        	 if (feature.getPointLocation() == null) {
                 LOGGER.warn("The foi with ID {} has no valid point!", feature.getId());
             } else {
	        	 // add feature
	            lookup.addFeature(feature);
	
	            // create station if not exists
	            Station station = metadata.getStationByFeature(feature);
	            if (station == null) {
	                station = new Station(feature);
	                station.setLocation(feature.getPointLocation());
	                metadata.addStation(station);
	                stations.add(station);
	            }
             }
		}
        return stations;
	}
	
	public List<Station> createStations(OperationResult result) throws IOException, ExceptionReport, OXFException {
		List<Station> stations = new ArrayList<Station>();
        TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
        List<SensorThingsFeatureOfInterest> parseSensorThingsFeatureOfInterest = decode(JSONUtils.loadStream(result.getIncomingResultAsAutoCloseStream()));
        for (SensorThingsFeatureOfInterest feature : parseSensorThingsFeatureOfInterest) {
        	 if (feature.getPointLocation() == null) {
                 LOGGER.warn("The foi with ID {} has no valid point!", feature.getId());
             } else {
	        	 // add feature
	            lookup.addFeature(feature);
	
	            // create station if not exists
	            Station station = metadata.getStationByFeature(feature);
	            if (station == null) {
	                station = new Station(feature);
	                station.setLocation(feature.getPointLocation());
	                metadata.addStation(station);
	                stations.add(station);
	            }
             }
		}
        return stations;
        
//        String id = null;
//        String label = null;
//        for (FeaturePropertyType featurePropertyType : foiResDoc.getGetFeatureOfInterestResponse().getFeatureMemberArray()) {
//            Point point = null;
//            XmlCursor xmlCursor = featurePropertyType.newCursor();
//            if (xmlCursor.toChild(new QName("http://www.opengis.net/samplingSpatial/2.0",
//                                            "SF_SpatialSamplingFeature"))) {
//                SFSamplingFeatureDocument samplingFeature = SFSamplingFeatureDocument.Factory.parse(xmlCursor.getDomNode());
//                SFSamplingFeatureType sfSamplingFeature = samplingFeature.getSFSamplingFeature();
//                id = sfSamplingFeature.getIdentifier().getStringValue();
//                if (sfSamplingFeature.getNameArray().length > 0) {
//                    label = sfSamplingFeature.getNameArray(0).getStringValue();
//                }
//                else {
//                    label = id;
//                }
//                point = createParsedPoint(sfSamplingFeature, referenceHelper);
//            }
//            else if (xmlCursor.toChild(new QName("http://www.opengis.net/waterml/2.0", "MonitoringPoint"))) {
//                MonitoringPointDocument monitoringPointDoc = MonitoringPointDocument.Factory.parse(xmlCursor.getDomNode());
//                MonitoringPointType monitoringPoint = monitoringPointDoc.getMonitoringPoint();
//                id = monitoringPoint.getIdentifier().getStringValue();
//                if (monitoringPoint.getNameArray().length > 0) {
//                    label = monitoringPoint.getNameArray(0).getStringValue();
//                }
//                else {
//                    label = id;
//                }
//                point = createParsedPoint(monitoringPoint, referenceHelper);
//            }
//            else {
//                LOGGER.error("Did not find supported feature members in the GetFeatureOfInterest response");
//            }
//            if (point == null) {
//                LOGGER.warn("The foi with ID {} has no valid point: {}", id, featurePropertyType.toString());
//            }
//            else {
//                // add feature
//                Feature feature = new Feature(id, metadata.getServiceUrl());
//                feature.setLabel(label);
//                lookup.addFeature(feature);
//
//                // create station if not exists
//                Station station = metadata.getStationByFeature(feature);
//                if (station == null) {
//                    station = new Station(feature);
//                    station.setLocation(point);
//                    metadata.addStation(station);
//                    stations.add(station);
//                }
//            }
//        }
//        return stations;
	}
	
	private Geometry parseFeature(JsonNode node) {
		JsonNode path = node.path(FEATURE);
		if (path != null) {
			return createParsedPoint(path, referenceHelper);
		}
		return null;
	}
	
}
