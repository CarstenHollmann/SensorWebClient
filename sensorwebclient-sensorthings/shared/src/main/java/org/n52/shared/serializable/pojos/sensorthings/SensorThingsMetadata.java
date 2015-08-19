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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.n52.oxf.ows.capabilities.Operation;
import org.n52.shared.Metadata;
import org.n52.shared.MetadataBuilder;
import org.n52.shared.requests.query.QueryParameters;
import org.n52.shared.sensorthings.decoder.SensorThingsConstants;
import org.n52.shared.serializable.pojos.sos.Feature;
import org.n52.shared.serializable.pojos.sos.SosTimeseries;
import org.n52.shared.serializable.pojos.sos.Station;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;

public class SensorThingsMetadata extends Metadata implements SensorThingsConstants, Serializable {

	private static final long serialVersionUID = 5445209463470572842L;
	
	private static final String DEFAULT_METADATA_HANDLER = "org.n52.server.sensorthings.da.SensorThingsMetadataHandler";
	
    private TimeseriesParametersLookup timeseriesParametersLookup;

    private HashMap<String, Station> stations = new HashMap<String, Station>();

    @SuppressWarnings("unused")
    private SensorThingsMetadata() {
        // for serialization
    }
    
    public SensorThingsMetadata(String serviceUrl, String version, String title) {
    	super(serviceUrl, title, version);
	}
    
    public SensorThingsMetadata(MetadataBuilder builder) {
    	super(builder);
    }
    
    @Override
    public String getDefaultMetadataHandler() {
    	return DEFAULT_METADATA_HANDLER;
    }

    public void addStation(Station station) {
        stations.put(station.getFeature().getFeatureId(), station);
    }

    public ArrayList<Station> getStations() {
        return new ArrayList<Station>(stations.values());
    }

    public SosTimeseries[] getMatchingTimeseries(QueryParameters parameters) {
        List<SosTimeseries> matchingTimeseries = new ArrayList<SosTimeseries>();
        for (Station station : stations.values()) {
            final String stationFilter = parameters.getStation();
            boolean hasStationFilter = stationFilter != null;
            if ( !hasStationFilter || station.getGlobalId().equals(stationFilter)) {
                for (SosTimeseries timeseries : station.getObservedTimeseries()) {
                    if (timeseries.matchesGlobalIds(parameters)) {
                        matchingTimeseries.add(timeseries);
                    }
                }
            }
        }
        return matchingTimeseries.toArray(new SosTimeseries[0]);
    }

    public Station getStationByTimeSeriesId(String timeseriesId) {
        for (Station station : stations.values()) {
            if (station.contains(timeseriesId)) {
                return station;
            }
        }
        return null;
    }

    public boolean containsStationWithTimeseriesId(String timeseriesId) {
        for (Station station : stations.values()) {
            if (station.contains(timeseriesId)) {
                return true;
            }
        }
        return false;
    }

    public Station getStationByTimeSeries(SosTimeseries timeseries) {
        for (Station station : stations.values()) {
            if (station.contains(timeseries)) {
                return station;
            }
        }
        return null;
    }

    public boolean containsTimeseriesWith(SosTimeseries timeseries) {
        for (Station station : stations.values()) {
            if (station.contains(timeseries)) {
                return true;
            }
        }
        return false;
    }

    public Station getStationByFeature(Feature feature) {
        return stations.get(feature.getFeatureId());
    }

    /**
     * @return a lookup helper for timeseries parameters.
     */
    public TimeseriesParametersLookup getTimeseriesParametersLookup() {
        timeseriesParametersLookup = timeseriesParametersLookup == null
            ? new TimeseriesParametersLookup()
            : timeseriesParametersLookup;
        return timeseriesParametersLookup;
    }
    
	public Operation getThingsOperation() {
		return new Operation(THINGS, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getLocatiopnsOperation() {
		return new Operation(LOCATIONS, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getDatastreamsOperation() {
		return new Operation(DATASTREAMS, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getHistoricalLocationsOperation() {
		return new Operation(HISTORICAL_LOCATIONS, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getSensorsOperation() {
		return new Operation(SENSORS, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getObservationsOperation() {
		return new Operation(OBSERVATIONS, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getObservedPropertiesOperation() {
		return new Operation(OBSERVED_PROPERTIES, getServiceUrl(), getServiceUrl());
	}
	
	public Operation getFeaturesOfInterestOperation() {
		return new Operation(FEATURES_OF_INTEREST, getServiceUrl(), getServiceUrl());
	}
    
    public SensorThingsMetadata clone() {
        SensorThingsMetadata clone = new SensorThingsMetadata(getServiceUrl(),
                                            getVersion(),
                                            getTitle());
        super.clone(clone);
        return clone;
    }

}
