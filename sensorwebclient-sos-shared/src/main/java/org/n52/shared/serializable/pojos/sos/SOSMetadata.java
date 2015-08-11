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
package org.n52.shared.serializable.pojos.sos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.n52.shared.Metadata;
import org.n52.shared.MetadataBuilder;
import org.n52.shared.requests.query.QueryParameters;

/**
 * A shared metadata representation for an SOS instance. An {@link SOSMetadata} is used from both (!) Client
 * side and Server side. Depending on if the SOS metadata representation is used on either Client sider or
 * Server side, attributes have to be set differently (see constructor notes) ! It is the developer's
 * responsibility to keep them in sync.
 *
 * TODO this above fact is based on historical reasons and have to refactored!
 */
public class SOSMetadata extends Metadata {

    private static final long serialVersionUID = -3721927620888635622L;
    
    private static final String DEFAULT_METADATA_HANDLER = "org.n52.server.da.oxf.DefaultMetadataHandler";

    private String sensorMLVersion;

    private String omVersion;
    
    private List<String> observationFormats;
    
    private List<String> procedureFormats;

    private TimeseriesParametersLookup timeseriesParametersLookup;

    private HashMap<String, Station> stations = new HashMap<String, Station>();

    @SuppressWarnings("unused")
    private SOSMetadata() {
    	super();
        // for serialization
    }

    public SOSMetadata(String url, String sosVersion, String sensorMLVersion, String omVersion, String title) {
    	super(url, title, sosVersion);
        this.sensorMLVersion = sensorMLVersion;
        this.omVersion = omVersion;
    }

    @Deprecated
    public SOSMetadata(String id, String title) {
        this(id);
        setTitle(title);
    }

    /**
     * @deprecated use {@link #SOSMetadata(String, String)}
     *
     * @see {@link #SOSMetadata(String, String)} to explicitly set version
     */
    @Deprecated
    public SOSMetadata(String serviceUrl) {
        setServiceUrl(serviceUrl);
    }

    /**
     * Use this constructor only for non-configurated SOS instances! Prefer using
     * {@link SOSMetadata#SOSMetadata(SOSMetadataBuilder)}.
     *
     * @param url
     *        the service URL
     * @param title
     *        A service title
     * @param version
     *        the supported version
     */
    public SOSMetadata(String url, String title, String version) {
    	super(url, title, version);
    }

    public SOSMetadata(MetadataBuilder builder) {
    	super(builder);
    }

    @Override
	public String getDefaultMetadataHandler() {
		return DEFAULT_METADATA_HANDLER;
	}

	/**
     * @return the configured SOS metadata handler or <code>null</code> when called from client side.
     */
    public String getSosMetadataHandler() {
        return getMetadataHandler();
    }

    public final void setSosMetadataHandler(String handler) {
        // is null when used on client side
        setMetadataHandler(handler != null ? handler.trim() : null);
    }

    public String getSosVersion() {
        return getVersion();
    }

    public String getSensorMLVersion() {
        return this.sensorMLVersion;
    }

    public String getOmVersion() {
        return this.omVersion;
    }

    public void setSosVersion(String sosVersion) {
        setVersion(sosVersion);
    }

    public void setSensorMLVersion(String sensorMLVersion) {
        this.sensorMLVersion = sensorMLVersion;
    }

    public void setOmVersion(String omVersion) {
        this.omVersion = omVersion;
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
    
    public List<String> getObservationFormats() {
		return observationFormats;
	}

    public void setObservationFormats(List<String> observationFormats) {
    	this.observationFormats = observationFormats;
	}
    
	public List<String> getProcedureFormats() {
		return procedureFormats;
	}

	public void setProcedureFormats(List<String> procedureFormats) {
		this.procedureFormats = procedureFormats;
	}

    public SOSMetadata clone() {
        SOSMetadata clone = new SOSMetadata(getServiceUrl(),
                                            getVersion(),
                                            this.sensorMLVersion,
                                            this.omVersion,
                                            getTitle());
        super.clone(clone);
        clone.observationFormats = this.observationFormats;
        clone.procedureFormats = this.procedureFormats;
        return clone;
    }


}
