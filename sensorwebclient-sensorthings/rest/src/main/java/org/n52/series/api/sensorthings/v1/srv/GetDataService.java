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
package org.n52.series.api.sensorthings.v1.srv;

import static org.n52.io.v1.data.TimeseriesData.newTimeseriesData;
import static org.n52.server.util.TimeUtil.createIso8601Formatter;
import static org.n52.shared.serializable.pojos.DesignOptions.createOptionsForGetFirstValue;
import static org.n52.shared.serializable.pojos.DesignOptions.createOptionsForGetLastValue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.client.service.TimeSeriesDataService;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesDataMetadata;
import org.n52.io.v1.data.TimeseriesValue;
import org.n52.io.v1.data.UndesignedParameterSet;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.ITime;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.oxf.valueDomains.time.TimePosition;
import org.n52.server.da.oxf.OperationAccessor;
import org.n52.server.da.oxf.ResponseExceedsSizeLimitException;
import org.n52.server.sensorthings.da.SensorThingsAdapter;
import org.n52.server.sensorthings.util.SensorThingsAdapterFactory;
import org.n52.shared.requests.TimeSeriesDataRequest;
import org.n52.shared.responses.TimeSeriesDataResponse;
import org.n52.shared.sensorthings.decoder.SensorThingsConstants;
import org.n52.shared.sensorthings.decoder.SensorThingsObservationDecoder;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.ReferenceValue;
import org.n52.shared.serializable.pojos.TimeseriesProperties;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsMetadata;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsObservation;
import org.n52.shared.serializable.pojos.sos.SosTimeseries;
import org.n52.web.BadRequestException;
import org.n52.web.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets data values from an SOS instance. Requested time series are aggregated
 * to a list of {@link TimeseriesProperties} and passed to a configured
 * {@link TimeSeriesDataService}. Data response will be enriched by further
 * metadata from each procedure measuring the requested time series.
 */
public class GetDataService extends DataService implements SensorThingsConstants {
	
	public static final String PARAM_FIRST = "getFirst";

	public static final String PARAM_LAST = "latest";
	
	private static final String DESC = "desc";
	
	private static final String ASC = "asc";

	static final Logger LOGGER = LoggerFactory.getLogger(GetDataService.class);

	private SimpleDateFormat dateFormat = createIso8601Formatter();
	
	/**
	 * @param parameterSet
	 *            containing request parameters.
	 * @return a time series result instance, identified by
	 *         {@link SosTimeseries#getTimeseriesId()}
	 */
	public TvpDataCollection getTimeSeriesFromParameterSet(
			UndesignedParameterSet parameterSet) {
		ArrayList<TimeseriesProperties> tsProperties = new ArrayList<TimeseriesProperties>();
		TvpDataCollection timeseriesCollection = prepareTimeseriesResults(parameterSet, tsProperties);
		return performTimeseriesDataRequest(timeseriesCollection, createDesignOptions(parameterSet, tsProperties));
	}
	
	//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams%286%29/Observations?$select=id,phenomenonTime,result
	private TvpDataCollection performTimeseriesDataRequest(TvpDataCollection timeSeriesResults, DesignOptions options) {
		try {
			TimeSeriesDataRequest tsRequest = new TimeSeriesDataRequest(options);
			TimeSeriesDataResponse timeSeriesData = getTimeSeriesData(tsRequest);
			Map<String, HashMap<Long, Double>> data = timeSeriesData
					.getPayloadData();

			for (String timeseriesId : timeSeriesResults.getAllTimeseries().keySet()) {
				TimeseriesProperties properties = getTimeseriesProperties(timeseriesId, options);
				GetDataInfos infos = new GetDataInfos(timeseriesId, properties, options);
				HashMap<Long, Double> values = data.get(timeseriesId);
				TimeseriesData timeseriesData = newTimeseriesData(values);
				if (properties.getReferenceValues() != null) {
					timeseriesData.setMetadata(createTimeseriesMetadata(infos));
				}
				timeSeriesResults
						.addNewTimeseries(timeseriesId, timeseriesData);
			}
		} catch (ResponseExceedsSizeLimitException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(
					"Could not get timeseries data for options: " + options, e);
		}
		return timeSeriesResults;
	}

	private TimeSeriesDataResponse getTimeSeriesData(TimeSeriesDataRequest tsRequest) throws OXFException {
		HashMap<String, HashMap<Long, Double>> allTimeSeries = new HashMap<String, HashMap<Long, Double>>();
		for (TimeseriesProperties properties : tsRequest.getOptions().getProperties()) {
			allTimeSeries.put(properties.getTimeseriesId(), getValues(getObservations(properties, tsRequest.getOptions())));
			
		}
		return new TimeSeriesDataResponse(allTimeSeries);
	}

	private HashMap<Long, Double> getValues(List<SensorThingsObservation> observations) {
		HashMap<Long, Double> map = new HashMap<Long, Double>();
		for (SensorThingsObservation o : observations) {
			if (o.getResult() instanceof Double) {
				Long millis = getTimeInMillis(o);
				if (millis != null) {
					map.put(getTimeInMillis(o), ((Double)o.getResult()).doubleValue());
				}
			}
		}
		return map;
	}

	private Long getTimeInMillis(SensorThingsObservation o) {
		ITime phenomenonTime = o.getPhenomenonTime();
		if (phenomenonTime instanceof TimePeriod) {
			TimePeriod tp = (TimePeriod)phenomenonTime;
			return DateTime.parse(tp.getEnd().toISO8601Format()).getMillis();
		} else if (phenomenonTime instanceof TimePosition) {
			return DateTime.parse(((TimePosition)phenomenonTime).toISO8601Format()).getMillis();
		}
		return null;
	}

	private List<SensorThingsObservation> getObservations(TimeseriesProperties properties, DesignOptions options) throws OXFException {
		String timeseriesId = properties.getTimeseriesId();
		SosTimeseries timeseries = properties.getTimeseries();
		String datastreamId = timeseries.getOfferingId();
		SensorThingsMetadata metadata = getMetadataForTimeseriesId(timeseriesId);
		
		SensorThingsAdapter adapter = SensorThingsAdapterFactory.createSensorThingsAdapter(metadata);
		ParameterContainer container = new ParameterContainer();
		container.addParameterShell(ID, datastreamId);
		container.addParameterShell(URI_PATH, OBSERVATIONS);
		// TODO add foi filter because a datastream can have multiple fois 
		checkForFirstLatest(container, options);
		OperationAccessor oa = new OperationAccessor(adapter, metadata.getDatastreamsOperation(), container);
		OperationResult result = oa.call();
		SensorThingsObservationDecoder decoder = new SensorThingsObservationDecoder(adapter);
		try {
			return decoder.decode(result, options);
		} catch (IOException e) {
			throw new OXFException(e);
		} catch (ExceptionReport e) {
			throw new OXFException(e);
		}
	}

	private void checkForFirstLatest(ParameterContainer container, DesignOptions options) throws OXFException {
		if (options.getTimeParam() != null && !options.getTimeParam().isEmpty()) {
			String ascDesc = null;
			if (PARAM_FIRST.equalsIgnoreCase(options.getTimeParam())) {
				ascDesc = ASC;
			} else if (PARAM_LAST.equalsIgnoreCase(options.getTimeParam())) {
				ascDesc = DESC;
			}
			if (ascDesc != null) {
				container.addParameterShell(ORDER_BY, PHENOMENON_TIME + " " + ascDesc);
				container.addParameterShell(TOP, 1);
			}
		}
	}

	private TimeseriesDataMetadata createTimeseriesMetadata(GetDataInfos infos) {
		HashMap<String, ReferenceValue> refValues = infos.getProperties()
				.getRefvalues();
		if (refValues == null || refValues.isEmpty()) {
			return null;
		}
		TimeseriesDataMetadata timeseriesMetadata = new TimeseriesDataMetadata();
		timeseriesMetadata.setReferenceValues(createReferenceValuesData(
				refValues, infos));
		return timeseriesMetadata;
	}

	private Map<String, TimeseriesData> createReferenceValuesData(
			HashMap<String, ReferenceValue> refValues, GetDataInfos infos) {
		Map<String, TimeseriesData> refValuesDataCollection = new HashMap<String, TimeseriesData>();
		for (String referenceValueId : refValues.keySet()) {
			ReferenceValue referenceValue = refValues.get(referenceValueId);
			TimeseriesValue[] referenceValues = referenceValue.getValues().length == 1 ? fitReferenceValuesForInterval(
					referenceValue, infos) : referenceValue.getValues();
			TimeseriesData timeseriesData = newTimeseriesData(referenceValues);
			refValuesDataCollection.put(referenceValue
					.getGeneratedGlobalId(infos.getTimeseriesId()),
					timeseriesData);
		}
		return !refValuesDataCollection.isEmpty() ? refValuesDataCollection
				: null;
	}

	private TimeseriesValue[] fitReferenceValuesForInterval(
			ReferenceValue referenceValue, GetDataInfos infos) {
		DesignOptions options = infos.getOptions();
		long begin = options.getBegin();
		long end = options.getEnd();

		/*
		 * We create artificial interval bounds for "one value" references to
		 * match the requested timeframe. This is needed to render the
		 * particular reference value in a chart.
		 */

		TimeseriesValue lastValue = referenceValue.getLastValue();
		TimeseriesValue from = new TimeseriesValue(begin, lastValue.getValue());
		TimeseriesValue to = new TimeseriesValue(end, lastValue.getValue());
		return new TimeseriesValue[] { from, to };
	}

	public TimeseriesValue getFirstValue(SosTimeseries timeseries) {
		TimeseriesProperties properties = createCondensedTimeseriesProperties(timeseries
				.getTimeseriesId());
		DesignOptions designOptions = createOptionsForGetFirstValue(properties);
		return performFirstOrLastValueRequest(properties, designOptions);
	}

	public TimeseriesValue getLastValue(SosTimeseries timeseries) {
		TimeseriesProperties properties = createCondensedTimeseriesProperties(timeseries
				.getTimeseriesId());
		DesignOptions designOptions = createOptionsForGetLastValue(properties);
		return performFirstOrLastValueRequest(properties, designOptions);
	}
 
	//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams(7)/Observations?$orderby=phenomenonTime asc&$top=1 for first
	//http://162.244.228.33:8080/OGCSensorThings/v1.0/Datastreams(7)/Observations?$orderby=phenomenonTime desc&$top=1 for last
	private TimeseriesValue performFirstOrLastValueRequest(TimeseriesProperties properties, DesignOptions options) {
		try {
			HashMap<Long, Double> values = getValues(getObservations(properties, options));
			
			for (Entry<Long, Double> entry : values.entrySet()) {
				return new TimeseriesValue(entry.getKey(), entry.getValue());
			}
			LOGGER.error("Server did not return the first/last value for timeseries '{}'.",
					properties.getTimeseriesId());
			return null;
		} catch (Exception e) {
			LOGGER.debug("Could not retrieve first or last value request. Probably not supported.");
			return null;
		}
	}

	protected ITime getTimeFrom(UndesignedParameterSet parameters) {
		Interval timespan = Interval.parse(parameters.getTimespan());
        Calendar beginPos = Calendar.getInstance();
        beginPos.setTimeInMillis(timespan.getStartMillis());
        Calendar endPos = Calendar.getInstance();
        endPos.setTimeInMillis(timespan.getEndMillis());
        String begin = dateFormat.format(beginPos.getTime());
        String end = dateFormat.format(endPos.getTime());
        return TimeFactory.createTime(begin + "/" + end);
    }

	
}
