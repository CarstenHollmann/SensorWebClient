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
package org.n52.server.da.oxf;

import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_FOI_SERVICE_PARAMETER;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_FOI_VERSION_PARAMETER;
import static org.n52.oxf.sos.adapter.SOSAdapter.GET_FEATURE_OF_INTEREST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.sampling.x20.SFSamplingFeatureDocument;
import net.opengis.sampling.x20.SFSamplingFeatureType;
import net.opengis.sos.x20.GetFeatureOfInterestResponseDocument;

import org.apache.xmlbeans.XmlObject;
import org.jfree.util.Log;
import org.n52.io.crs.CRSUtils;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ServiceDescriptor;
import org.n52.oxf.ows.capabilities.Contents;
import org.n52.oxf.ows.capabilities.IBoundingBox;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.sos.adapter.SOSAdapter;
import org.n52.oxf.sos.capabilities.ObservationOffering;
import org.n52.server.da.MetadataHandler;
import org.n52.server.mgmt.ConfigurationContext;
import org.n52.server.parser.ConnectorUtils;
import org.n52.server.util.SosAdapterFactory;
import org.n52.shared.Metadata;
import org.n52.shared.serializable.pojos.sos.Feature;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.SosService;
import org.n52.shared.serializable.pojos.sos.SosTimeseries;
import org.n52.shared.serializable.pojos.sos.Station;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSosMetadataHandler extends MetadataHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSosMetadataHandler.class);
	
    protected AbstractSosMetadataHandler(SOSMetadata metadata) {
		super(metadata);
	}
    
    @Override
    public IServiceAdapter createAdapter(Metadata metadata) {
    	return createSosAdapter((SOSMetadata)metadata);
    }
    
    protected void infoLogServiceSummary(SOSMetadata metadata) {
        int timeseriesCount = 0;
        int stationCount = metadata.getStations().size();
        for (Station station : metadata.getStations()) {
            timeseriesCount += station.getObservedTimeseries().size();
        }
        String serviceName = metadata.getConfiguredItemName();
        LOGGER.info("{} cached: {} stations with {} timeseries.", serviceName, stationCount, timeseriesCount);
    }
    
    protected Metadata initMetadata() {
        String sosUrl = getServiceUrl();
        String sosVersion = getServiceVersion();
        
        ServiceDescriptor serviceDescriptor = initServiceDescriptor();
        String sosTitle = serviceDescriptor.getServiceIdentification().getTitle();
        String omResponseFormat = ConnectorUtils.getResponseFormat(serviceDescriptor, "om");
        String smlVersion = ConnectorUtils.getSMLVersion(serviceDescriptor, sosVersion);
        List<String> observationFormats =  ConnectorUtils.getResponseFormats(serviceDescriptor);
        List<String> procedureFormats = ConnectorUtils.getProcedureDescriptionFormats(serviceDescriptor, sosVersion);

        // set defaults if format/version could not be determined from capabilities
        if (omResponseFormat == null) {
            omResponseFormat = "http://www.opengis.net/om/2.0";
        }
        if (smlVersion == null) {
            smlVersion = "http://www.opengis.net/sensorML/1.0.1";
        }
        try {
            metadata = ConfigurationContext.getServiceMetadatas().get(sosUrl);
            if (metadata != null) {
            	SOSMetadata sosMetadata = (SOSMetadata)metadata;
                sosMetadata.setTitle(sosTitle);
                sosMetadata.setSensorMLVersion(smlVersion);
                sosMetadata.setOmVersion(omResponseFormat);
                sosMetadata.setObservationFormats(observationFormats);
                sosMetadata.setProcedureFormats(procedureFormats);
                sosMetadata.setSosVersion(sosVersion);
                sosMetadata.setInitialized(true);
            } else {
                metadata = new SOSMetadata(sosUrl, sosVersion, smlVersion, omResponseFormat, sosTitle);
                ConfigurationContext.initializeMetadata(metadata);
            }
        } catch (Exception e) {
            Log.error("Cannot cast SOSMetadata", e);
        }
        return metadata;
    }
    
	private SOSMetadata getSOSMetadata() {
		return (SOSMetadata)metadata;
	}

	protected SOSAdapter createSosAdapter(SOSMetadata metadata) {
        return SosAdapterFactory.createSosAdapter(metadata);
    }
    
    protected SOSAdapter getSosAdapter() {
        return (SOSAdapter) getAdapter();
    }

    protected void setSosAdapter(SOSAdapter sosAdapter) {
    	setAdapter(sosAdapter);
    }
    
    protected Collection<SosTimeseries> createObservingTimeseries(String sosUrl) throws OXFException {
        // association: Offering - FOIs
        Map<String, String[]> offeringFoiMap = new HashMap<String, String[]>();

        // association: Offering - Procedures
        Map<String, String[]> offeringProcMap = new HashMap<String, String[]>();

        // association: Offering - Phenomenons
        Map<String, String[]> offeringPhenMap = new HashMap<String, String[]>();

        HashSet<String> featureIds = new HashSet<String>();

        Contents contents = getServiceDescriptor().getContents();
        for (int i = 0; i < contents.getDataIdentificationCount(); i++) {
            ObservationOffering offering = (ObservationOffering) contents.getDataIdentification(i);

            updateBBox(offering);

            String offeringID = offering.getIdentifier();

            // associate:
            String[] procArray = getProceduresFor(offering);
            offeringProcMap.put(offeringID, procArray);

            // associate:
            String[] phenArray = offering.getObservedProperties();
            offeringPhenMap.put(offeringID, phenArray);

            // associate:
            String[] foiArray = offering.getFeatureOfInterest();
            offeringFoiMap.put(offeringID, foiArray);

            // iterate over fois to delete double entries for the request
            for (int j = 0; j < foiArray.length; j++) {

                /*
                 * TODO perform GetFOI operations if capabilities does not
                 * list any features but offer a GetFOI operation.
                 */

                featureIds.add(foiArray[j]);
            }
        }

        TimeseriesParametersLookup lookup = getSOSMetadata().getTimeseriesParametersLookup();

        // add fois
        for (String featureId : featureIds) {
            lookup.addFeature(new Feature(featureId, sosUrl));
        }

        Collection<SosTimeseries> allObservedTimeseries = new ArrayList<SosTimeseries>();
        // FOI -> Procedure -> Phenomenon
        for (String offeringId : offeringFoiMap.keySet()) {
            for (String procedure : offeringProcMap.get(offeringId)) {
                if (procedure.contains("urn:ogc:generalizationMethod:")) {
                	getMetadata().setCanGeneralize(true);
                }
                else {
                    for (String phenomenon : offeringPhenMap.get(offeringId)) {
                        /*
                         * add a timeseries for each procedure-phenomenon-offering constellation. One of such
                         * constellation may be later related to n features.
                         */
                        SosTimeseries timeseries = new SosTimeseries();
                        timeseries.setPhenomenon(new Phenomenon(phenomenon, sosUrl));
                        timeseries.setProcedure(new Procedure(procedure, sosUrl));
                        timeseries.setOffering(new Offering(offeringId, sosUrl));
                        timeseries.setSosService(new SosService(getMetadata().getServiceUrl(), getMetadata().getVersion()));
                        timeseries.getSosService().setLabel(getMetadata().getTitle());
                        allObservedTimeseries.add(timeseries);
                    }
                    // add procedures
                    lookup.addProcedure(new Procedure(procedure, sosUrl));
                    for (String phenomenonId : offeringPhenMap.get(offeringId)) {
                        lookup.addPhenomenon(new Phenomenon(phenomenonId, sosUrl));
                    }
                }
            }
            // add offering
            lookup.addOffering(new Offering(offeringId, sosUrl));
        }
        return allObservedTimeseries;
    }
    
    /**
     * Creates an {@link CRSUtils} according to metadata settings (e.g. if XY axis order shall be enforced
     * during coordinate transformation).
     *
     * @param metadata
     *        the SOS metadata containing SOS instance configuration.
     */
    protected CRSUtils createReferencingHelper() {
        if (getMetadata().isForceXYAxisOrder()) {
            return CRSUtils.createEpsgForcedXYAxisOrder();
        }
        else {
            return CRSUtils.createEpsgStrictAxisOrder();
        }
    }

	protected Collection<String> getFoisByProcedure(String procedure)
            throws OXFException {
        ArrayList<String> fois = new ArrayList<String>();
        String url = getMetadata().getServiceUrl();
        try {
            ParameterContainer container = new ParameterContainer();
            container.addParameterShell(GET_FOI_SERVICE_PARAMETER, "SOS");
            container.addParameterShell(GET_FOI_VERSION_PARAMETER, getMetadata().getVersion());
            container.addParameterShell("procedure", procedure);
            Operation operation = new Operation(GET_FEATURE_OF_INTEREST, url, url);
            OperationResult result = getSosAdapter().doOperation(operation, container);
            XmlObject foiResponse = XmlObject.Factory.parse(result
                    .getIncomingResultAsAutoCloseStream());
            if (foiResponse instanceof GetFeatureOfInterestResponseDocument) {
                GetFeatureOfInterestResponseDocument foiResDoc = (GetFeatureOfInterestResponseDocument) foiResponse;
                for (FeaturePropertyType featurePropertyType : foiResDoc
                        .getGetFeatureOfInterestResponse()
                        .getFeatureMemberArray()) {
                    SFSamplingFeatureDocument samplingFeature = SFSamplingFeatureDocument.Factory
                            .parse(featurePropertyType.xmlText());
                    SFSamplingFeatureType sfSamplingFeature = samplingFeature
                            .getSFSamplingFeature();
                    fois.add(sfSamplingFeature.getIdentifier()
                            .getStringValue());
                }
            }
            else {
                throw new OXFException("No valid GetFeatureOfInterestResponse");
            }
        }
        catch (Exception e) {
            LOGGER.error("Error while send GetFeatureOfInterest: "
                    + e.getCause());
            throw new OXFException(e);
        }
        return fois;
    }
	
	protected void updateBBox(ObservationOffering offering) {
        IBoundingBox sosBbox = null;
        sosBbox = ConnectorUtils.createBbox(sosBbox, offering);

        try {
            if (sosBbox != null && !sosBbox.getCRS().startsWith("EPSG")) {
                String tmp = "EPSG:"
                        + sosBbox.getCRS().split(":")[sosBbox.getCRS()
                                .split(":").length - 1];
                metadata.setSrs(tmp);
            }
            else {
            	metadata.setSrs(sosBbox.getCRS());
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not insert spatial metadata", e);
        }
    }

}
