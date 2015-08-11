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
package org.n52.server.sensorthings.da;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.util.Log;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.server.da.MetadataHandler;
import org.n52.server.da.oxf.OperationAccessor;
import org.n52.server.mgmt.ConfigurationContext;
import org.n52.server.sensorthings.util.SensorThingsAdapterFactory;
import org.n52.shared.Metadata;
import org.n52.shared.sensorthings.decoder.SensorThingsConstants;
import org.n52.shared.sensorthings.decoder.SensorThingsDatastreamDecoder;
import org.n52.shared.sensorthings.decoder.SensorThingsFeatureOfInterestDecoder;
import org.n52.shared.sensorthings.decoder.SensorThingsObservedPropertyDecoder;
import org.n52.shared.sensorthings.decoder.SensorThingsSensorDecoder;
import org.n52.shared.sensorthings.decoder.SensorThingsThingsDecoder;
import org.n52.shared.serializable.pojos.TimeseriesProperties;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsDatastream;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsFeatureOfInterest;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsMetadata;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsObservedProperty;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsSensor;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsThings;
import org.n52.shared.serializable.pojos.sos.Category;
import org.n52.shared.serializable.pojos.sos.Feature;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SosService;
import org.n52.shared.serializable.pojos.sos.SosTimeseries;
import org.n52.shared.serializable.pojos.sos.Station;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;

import com.vividsolutions.jts.geom.Geometry;

public class SensorThingsMetadataHandler extends MetadataHandler implements SensorThingsConstants {

	public SensorThingsMetadataHandler(SensorThingsMetadata metadata) {
		super(metadata);
	}

	@Override
	public Metadata performMetadataCompletion() throws Exception {
		SensorThingsMetadata metadata = initMetadata();
		final Collection<SosTimeseries> observingTimeseries = createObservingTimeseries(getServiceVersion());

//        normalizeDefaultCategories(observingTimeseries);
		
		metadata.setInitialized(true);
        return metadata;
	}

	private SensorThingsMetadata initMetadata() {
		String url = getServiceUrl();
        String version = getServiceVersion();
        
        initServiceDescriptor();

        try {
            metadata = ConfigurationContext.getServiceMetadatas().get(url);
            if (metadata != null) {
            	SensorThingsMetadata localMetadata = (SensorThingsMetadata) metadata;
            	localMetadata.setVersion(version);
            	localMetadata.setInitialized(true);
            } else {
                metadata = new SensorThingsMetadata(url, version, "");
                ConfigurationContext.initializeMetadata(metadata);
            }
        } catch (Exception e) {
            Log.error("Cannot cast SOSMetadata", e);
        }
        return (SensorThingsMetadata)metadata;
	}

	@Override
	public Metadata updateMetadata(Metadata metadata) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assembleTimeseriesMetadata(TimeseriesProperties properties)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected IServiceAdapter createAdapter(Metadata metadata) {
		return createSensorThingsAdapter((SensorThingsMetadata)metadata);
	}
	
	private SensorThingsAdapter createSensorThingsAdapter(SensorThingsMetadata metadata) {
		return SensorThingsAdapterFactory.createSensorThingsAdapter(metadata);
	}
	
	protected Collection<SosTimeseries> createObservingTimeseries(String sosUrl) throws OXFException {
		IServiceAdapter adapter = createAdapter(metadata);
//		List<Station> createStations = getStations(adapter);
		
		Map<Long, SensorThingsThings> thingsMap = new HashMap<Long, SensorThingsThings>();
		Map<Long, SensorThingsSensor> sensorsMap = new HashMap<Long, SensorThingsSensor>();
		Map<Long, SensorThingsObservedProperty> obsPropsMap = new HashMap<Long, SensorThingsObservedProperty>();
		Map<Long, SensorThingsFeatureOfInterest> featuresMap = new HashMap<Long, SensorThingsFeatureOfInterest>();
		
		Map<Long, Long> datastreamThing = new HashMap<Long, Long>();
		Map<Long, Long> datastreamSensor = new HashMap<Long, Long>();
		Map<Long, Long> datastreamObsProp = new HashMap<Long, Long>();
		Map<Long, Set<Long>> datastreamFeatures = new HashMap<Long, Set<Long>>();
		
		List<SensorThingsDatastream> datastreams = getDatastreams(adapter);
		for (SensorThingsDatastream datastream : datastreams) {
			updateBBox(datastream.getObservedArea());
			addThingsToMap(datastream.getId(), getThingForDatastream(adapter, datastream), thingsMap, datastreamThing);
			addSensorsToMap(datastream.getId(),  getSensorForDatastream(adapter, datastream), sensorsMap, datastreamSensor);
			addObservedPropertesToMap(datastream.getId(), getObservedPropertyForDatastream(adapter, datastream), obsPropsMap, datastreamObsProp);
			addFeaturesToMap(datastream.getId(), getFeaturesForDatastream(adapter, datastream), featuresMap, datastreamFeatures);
		}
		List<Station> stations = getStations(featuresMap.values(), adapter);
		
		TimeseriesParametersLookup lookup = ((SensorThingsMetadata)getMetadata()).getTimeseriesParametersLookup();
		for (SensorThingsSensor sensor : sensorsMap.values()) {
			lookup.addProcedure(sensor);
		}
		for (SensorThingsObservedProperty obsProp : obsPropsMap.values()) {
			 lookup.addPhenomenon(obsProp);
			 lookup.addOffering(new Offering(obsProp.getPhenomenonId(), sosUrl));
		}
		Collection<SosTimeseries> allObservedTimeseries = new ArrayList<SosTimeseries>();
		for (SensorThingsDatastream datastream : datastreams) {
			datastream.getId();
			SosTimeseries timeseries = new SosTimeseries();
			Phenomenon phenomenon = getPhenomenon(datastream.getId(), datastreamObsProp, obsPropsMap);
            timeseries.setPhenomenon(phenomenon);
            timeseries.setProcedure(getProcedure(datastream.getId(), datastreamSensor, sensorsMap));
            timeseries.setOffering(new Offering(phenomenon.getPhenomenonId(), sosUrl));
            timeseries.setCategory(getCategory(datastream.getId(), datastreamThing, thingsMap));
            timeseries.setSosService(new SosService(getMetadata().getServiceUrl(), getMetadata().getVersion()));
            timeseries.getSosService().setLabel(getMetadata().getTitle());
            allObservedTimeseries.add(timeseries);
            for (Long featureId : datastreamFeatures.get(datastream.getId())) {
            	SensorThingsFeatureOfInterest feature = featuresMap.get(featureId);
            	if (!lookup.containsFeature(Long.toString(featureId))) {
                    lookup.addFeature(feature);
                }
            	timeseries.setFeature(feature);
            	Station station = getSensorThingsMetadata().getStationByFeature(feature);
            	if (station == null) {
            		station = new Station(feature);
                    station.setLocation(feature.getPointLocation());
                    getSensorThingsMetadata().addStation(station);
            	}
                station.addTimeseries(timeseries);
			}
		}
        return allObservedTimeseries;
    }
	
	private SensorThingsMetadata getSensorThingsMetadata() {
		return (SensorThingsMetadata)metadata;
	}

	private Category getCategory(long id, Map<Long, Long> datastreamThing,
			Map<Long, SensorThingsThings> thingsMap) {
		return thingsMap.get(datastreamThing.get(id));
	}

	private Procedure getProcedure(long id, Map<Long, Long> datastreamSensor,
			Map<Long, SensorThingsSensor> sensorsMap) {
		return sensorsMap.get(datastreamSensor.get(id));
	}

	private Phenomenon getPhenomenon(long id, Map<Long, Long> datastreamObsProp,
			Map<Long, SensorThingsObservedProperty> obsPropsMap) {
		return obsPropsMap.get(datastreamObsProp.get(id));
	}

	private void addThingsToMap(long id, List<SensorThingsThings> things,
			Map<Long, SensorThingsThings> thingsMap, Map<Long, Long> datastreamThing) {
		for (SensorThingsThings thing : things) {
			thingsMap.put(thing.getId(), thing);
			datastreamThing.put(id, thing.getId());
		}
	}
	
	private void addSensorsToMap(long id, List<SensorThingsSensor> sensors,
			Map<Long, SensorThingsSensor> sensorsMap, Map<Long, Long> datastreamSensor) {
		for (SensorThingsSensor sensor : sensors) {
			sensorsMap.put(sensor.getId(), sensor);
			datastreamSensor.put(id, sensor.getId());
		}
	}
	
	private void addObservedPropertesToMap(long id, List<SensorThingsObservedProperty> observedProperties,
			Map<Long, SensorThingsObservedProperty> obsPropsMap, Map<Long, Long> datastreamObsProp) {
		for (SensorThingsObservedProperty obsProp : observedProperties) {
			obsPropsMap.put(obsProp.getId(), obsProp);
			datastreamObsProp.put(id, obsProp.getId());
		}
	}

	private void addFeaturesToMap(long id, List<SensorThingsFeatureOfInterest> featuresForDatastream,
			Map<Long, SensorThingsFeatureOfInterest> featuresMap, Map<Long, Set<Long>> datastreamFeatures) {
		for (SensorThingsFeatureOfInterest feature : featuresForDatastream) {
			featuresMap.put(feature.getId(), feature);
			Set<Long> values;
			if (datastreamFeatures.containsKey(id)) {
				datastreamFeatures.get(id).add(feature.getId());
			} else {
				values = new HashSet<Long>();
				values.add(feature.getId());
				datastreamFeatures.put(id, values);
			}
		}
	}

	private List<SensorThingsFeatureOfInterest> getFeaturesForDatastream(IServiceAdapter adapter, SensorThingsDatastream datastream) throws OXFException {
		// //http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams%287%29/Observations?$select=Obervations/FeatureOfInterest&$expand=FeatureOfInterest
		ParameterContainer container = new ParameterContainer();
		container.addParameterShell(ID, Long.toString(datastream.getId()));
		container.addParameterShell(URI_PATH, OBSERVATIONS);
		container.addParameterShell(SELECT, OBSERVATIONS + "/" + FEATURE_OF_INTEREST);
		container.addParameterShell(EXPAND, FEATURE_OF_INTEREST);
		OperationAccessor oa = new OperationAccessor(adapter, getDatastreamsOperation(), container);
		OperationResult result = oa.call();
		SensorThingsFeatureOfInterestDecoder decoder = new SensorThingsFeatureOfInterestDecoder(adapter, metadata);
		try {
			return decoder.decode(result);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
	}

	private List<SensorThingsObservedProperty> getObservedPropertyForDatastream(IServiceAdapter adapter, SensorThingsDatastream datastream) throws OXFException {
		//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams%287%29?$select=ObservedProperty&$expand=ObservedProperty
		ParameterContainer container = new ParameterContainer();
		container.addParameterShell(ID, Long.toString(datastream.getId()));
		container.addParameterShell(SELECT, OBSERVED_PROPERTY);
		container.addParameterShell(EXPAND, OBSERVED_PROPERTY);
		OperationAccessor oa = new OperationAccessor(adapter, getDatastreamsOperation(), container);
		OperationResult result = oa.call();
		SensorThingsObservedPropertyDecoder decoder = new SensorThingsObservedPropertyDecoder(adapter, metadata.getServiceUrl());
		try {
			return decoder.decode(result);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
	}

	private List<SensorThingsSensor> getSensorForDatastream(IServiceAdapter adapter, SensorThingsDatastream datastream) throws OXFException {
		//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams%287%29?$select=Sensor&$expand=Sensor
		ParameterContainer container = new ParameterContainer();
		container.addParameterShell(ID, Long.toString(datastream.getId()));
		container.addParameterShell(SELECT, SENSOR);
		container.addParameterShell(EXPAND, SENSOR);
		OperationAccessor oa = new OperationAccessor(adapter, getDatastreamsOperation(), container);
		OperationResult result = oa.call();
		SensorThingsSensorDecoder decoder = new SensorThingsSensorDecoder(adapter, metadata.getServiceUrl());
		try {
			return decoder.decode(result);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
	}

	private List<SensorThingsThings> getThingForDatastream(IServiceAdapter adapter, SensorThingsDatastream datastream) throws OXFException {
		//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams%287%29?$select=Thing&$expand=Thing
		ParameterContainer container = new ParameterContainer();
		container.addParameterShell(ID, Long.toString(datastream.getId()));
		container.addParameterShell(SELECT, THING);
		container.addParameterShell(EXPAND, THING);
		OperationAccessor oa = new OperationAccessor(adapter, getDatastreamsOperation(), container);
		OperationResult result = oa.call();
		SensorThingsThingsDecoder decoder = new SensorThingsThingsDecoder(adapter, metadata.getServiceUrl());
		try {
			return decoder.decode(result);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
		
	}

	private List<SensorThingsDatastream> getDatastreams(IServiceAdapter adapter) throws OXFException {
		OperationAccessor oa = new OperationAccessor(adapter, getDatastreamsOperation(), new ParameterContainer());
		OperationResult result = oa.call();
		SensorThingsDatastreamDecoder decoder = new SensorThingsDatastreamDecoder(adapter);
		try {
			return decoder.decode(result);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
		//http://162.244.228.33:8080/OGCSensorThings/v1.0/Observations?$select=FeatureOfInterest&$expand=FeatureOfInterest
		//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams%287%29/Observations?$select=Obervations/FeatureOfInterest&$expand=FeatureOfInterest
	}

	private List<Station> getStations(Collection<SensorThingsFeatureOfInterest> collection, IServiceAdapter adapter) throws OXFException {
		SensorThingsFeatureOfInterestDecoder decoder = new SensorThingsFeatureOfInterestDecoder(adapter, metadata);
		if (collection != null && !collection.isEmpty()) {
			return decoder.createStations(collection); 
		}
		OperationAccessor oa = new OperationAccessor(adapter, getFeaturesOfInterestOperation(), new ParameterContainer());
		OperationResult result = oa.call();
		try{
			return decoder.createStations(result);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
	}

	private void updateBBox(Geometry observedArea) {
		// TODO Auto-generated method stub
		
	}

	public Operation getThingsOperation() {
		return new Operation(THINGS, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getLocatiopnsOperation() {
		return new Operation(LOCATIONS, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getDatastreamsOperation() {
		return new Operation(DATASTREAMS, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getHistoricalLocationsOperation() {
		return new Operation(HISTORICAL_LOCATIONS, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getSensorsOperation() {
		return new Operation(SENSORS, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getObservationsOperation() {
		return new Operation(OBSERVATIONS, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getObservedPropertiesOperation() {
		return new Operation(OBSERVED_PROPERTIES, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
	public Operation getFeaturesOfInterestOperation() {
		return new Operation(FEATURES_OF_INTEREST, metadata.getServiceUrl(), metadata.getServiceUrl());
	}
	
}
