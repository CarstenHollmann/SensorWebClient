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
package org.n52.server.da;

import java.util.Collection;

import org.n52.io.crs.CRSUtils;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.ows.ServiceDescriptor;
import org.n52.oxf.ows.capabilities.IBoundingBox;
import org.n52.oxf.sos.capabilities.ObservationOffering;
import org.n52.server.parser.ConnectorUtils;
import org.n52.shared.Metadata;
import org.n52.shared.serializable.pojos.TimeseriesProperties;
import org.n52.shared.serializable.pojos.sos.Category;
import org.n52.shared.serializable.pojos.sos.SosTimeseries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetadataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataHandler.class);

    private ServiceDescriptor serviceDescriptor;

    private IServiceAdapter adapter;

    protected Metadata metadata;

    protected MetadataHandler(Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata must not be null.");
        }
        this.metadata = metadata;
    }

    protected String getServiceUrl() {
        return metadata.getServiceUrl();
    }

    protected String getServiceVersion() {
        return metadata.getVersion();
    }

    public abstract Metadata performMetadataCompletion() throws Exception;

    /**
     * Assembles timeseries' metadata and sets it to the passed {@link TimeseriesProperties} container.<br/>
     * <br/>
     * Abstracting the actual information source enables the implementor to either assemble metadata from
     * different sources (e.g. via DescribeSensor) or even from multiple information sources.
     *
     * @param properties
     *        the container to be assembled with available metadata.
     * @throws Exception
     *         if assembling metadata fails.
     */
    public abstract void assembleTimeseriesMetadata(final TimeseriesProperties properties) throws Exception;

    protected abstract IServiceAdapter createAdapter(Metadata metadata);

	protected Metadata getMetadata() {
		return metadata;
	}
	
    protected ServiceDescriptor initServiceDescriptor() {
    	this.serviceDescriptor = ConnectorUtils.getServiceDescriptor(getServiceUrl(), getAdapter());
    	
		return serviceDescriptor;
	}

    protected String[] getProceduresFor(ObservationOffering offering) {
        return offering.getProcedures();
    }

    protected void normalizeDefaultCategories(Collection<SosTimeseries> observingTimeseries) {
        for (SosTimeseries timeseries : observingTimeseries) {
            String phenomenon = timeseries.getPhenomenonId();
            String serviceUrl = timeseries.getServiceUrl();
            timeseries.setCategory(new Category(phenomenon, serviceUrl));
        }
    }

    protected IServiceAdapter getAdapter() {
        if (adapter == null) {
            adapter = createAdapter(metadata);
        }
        return adapter;
    }

    protected void setAdapter(IServiceAdapter sosAdapter) {
        this.adapter = sosAdapter;
    }

    /**
     * Creates an {@link CRSUtils} according to metadata settings (e.g. if XY axis order shall be enforced
     * during coordinate transformation).
     *
     * @param metadata
     *        the SOS metadata containing SOS instance configuration.
     */
    protected CRSUtils createReferencingHelper() {
        if (metadata.isForceXYAxisOrder()) {
            return CRSUtils.createEpsgForcedXYAxisOrder();
        }
        else {
            return CRSUtils.createEpsgStrictAxisOrder();
        }
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

}
