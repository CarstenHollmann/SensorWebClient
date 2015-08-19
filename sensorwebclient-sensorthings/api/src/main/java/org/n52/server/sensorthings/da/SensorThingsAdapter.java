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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.IServiceAdapter;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.ServiceDescriptor;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.ows.capabilities.OperationsMetadata;
import org.n52.oxf.request.RequestParameters;
import org.n52.oxf.sos.util.SosUtil;
import org.n52.oxf.util.web.GzipEnabledHttpClient;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.ProxyAwareHttpClient;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.n52.shared.sensorthings.decoder.SensorThingsServiceDecoder;

public class SensorThingsAdapter implements IServiceAdapter {
	
	 /**
     * Description of the SensorThingsAdapter
     */
    public static final String DESCRIPTION = "This Class implements the Service Adapter Interface and is"
            + "an SensorThings API Adapter for the OXF Framework";

    /**
     * The name of the service operation which returns the data to be added to a map view as a layer.
     */
    public static final String RESOURCE_OPERATION = "Datastream";
	
	/**
     * the schema version this adapter instance shall work with.
     */
    protected String serviceVersion = null;
	
    private SensorThingsRequestBuilder requestBuilder;

    private HttpClient httpClient;

	/**
     * @param serviceVersion
     *        the schema version for which this adapter instance shall be initialized.
     */
    public SensorThingsAdapter(final String serviceVersion) {
        this(serviceVersion, (SensorThingsRequestBuilder) null);
    }

    /**
     * @param serviceVersion
     *        the schema version for which this adapter instance shall be initialized.
     * @param httpclient
     *        the (decorated) {@link HttpClient} to use for service connections.
     */
    public SensorThingsAdapter(final String serviceVersion, final HttpClient httpclient) {
        this(serviceVersion, (SensorThingsRequestBuilder) null);
        setHttpClient(httpclient); // override simple client
    }

    /**
     * Allows to create an SensorThingsAdapter with custom (non-default) instance of {@link SensorThingsRequestBuilder}.<br>
     * <br>
     * By default the created instance will use a {@link SimpleHttpClient} for service communication. If
     * further features are needed the {@link SimpleHttpClient} can be decorated with further configuration
     * setups like a {@link GzipEnabledHttpClient} or a {@link ProxyAwareHttpClient}.
     *
     * @param serviceVersion
     *        the schema version for which this adapter instance shall be initialized.
     * @param requestBuilder
     *        a custom request builder implementation, if <code>null</code> a default builder will be used
     *        (according to the given version).
     * @see SensorThingsRequestBuilder
     */
    public SensorThingsAdapter(final String serviceVersion, final SensorThingsRequestBuilder requestBuilder) {
        httpClient = new SimpleHttpClient();
        this.serviceVersion = serviceVersion;
        if (requestBuilder == null) {
            this.requestBuilder = SensorThingsRequestBuilderFactory.generateRequestBuilder(serviceVersion);
        }
        else {
            this.requestBuilder = requestBuilder;
        }
    }

    /**
     * @param requestBuilder
     *        a custom {@link SensorThingsRequestBuilder} implementation the {@link SensorThingsAdapter} shall use.
     */
    public void setRequestBuilder(final SensorThingsRequestBuilder requestBuilder) {
        if (requestBuilder != null) {
            this.requestBuilder = requestBuilder;
        }
    }
    
    /**
     * Sets a custom {@link HttpClient} for service communication. A {@link SimpleHttpClient} can be decorated
     * to enable for example GZIP encoding (setting Accept-Encoding plus GZIP decompressing) or being aware of
     * proxies.
     *
     * @param httpClient
     *        a customly configured {@link HttpClient} the {@link SensorThingsAdapter} shall use.
     * @see ProxyAwareHttpClient
     * @see GzipEnabledHttpClient
     */
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * initializes the ServiceDescriptor by requesting the SensorThings API.
     *
     * @param url
     *        the base URL of the SensorThings API
     * @param serviceVersion
     *        the schema version to which the service description shall be conform.
     *
     * @return the ServiceDescriptor based on the retrieved SensorThings API response
     *
     * @throws ExceptionReport
     *         if service side exception occurs
     * @throws OXFException
     *         if internal exception occurs
     *
     */
	@Override
	public ServiceDescriptor initService(String serviceURL) throws ExceptionReport, OXFException {
		OperationsMetadata om;
		try {
			om = new SensorThingsServiceDecoder(this).decode(doOperation(new Operation("Service", serviceURL, serviceURL), new ParameterContainer()));
		} catch (IOException e) {
			 throw new OXFException(e);
		}
		SensorThingsServiceDescriptor stsd = new SensorThingsServiceDescriptor("v1.0", null, null, om, null);
		return stsd;
	}

	/**
    *
    * @param operation
    *        the operation which the adapter has to execute on the service. this operation includes also the
    *        parameter values.
    *
    * @param parameters
    *        Map which contains the parameters of the operation and the corresponding parameter values
    *
    * @throws ExceptionReport
    *         Report which contains the service sided exceptions
    *
    * @throws OXFException
    *         if the sending of the post message failed.<br>
    *         if the specified Operation is not supported.
    *
    * @return the result of the executed operation
    */
	@Override
	public OperationResult doOperation(Operation operation, ParameterContainer parameters) throws ExceptionReport,
			OXFException {
		try {
			String uri = requestBuilder.buildRequestUrl(operation, parameters);
			final RequestParameters request = requestBuilder.buildRequest(operation, parameters);
			final HttpResponse httpResponse = httpClient.executeGet(uri, request);
			final HttpEntity responseEntity = httpResponse.getEntity();
			OperationResult result = new OperationResult( responseEntity.getContent(), parameters, request.toString());
			return result;
		} catch (final HttpClientException e) {
			throw new OXFException("Sending request failed.", e);
		} catch (final IOException e) {
			throw new OXFException("Could not create OperationResult.", e);
		}
	}

	/**
     * returns the ResourceOperationName
     *
     * @return The name of the service operation which returns the data to be added to a map view as a layer.
     */
    @Override
	public String getResourceOperationName() {
        return RESOURCE_OPERATION;
    }

    /**
     * returns the description of this Service Adapter
     *
     * @return String the description of the adapter
     */
    @Override
	public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * returns the type of the service which is connectable by this ServiceAdapter
     *
     * @return String the type of service
     */
    @Override
	public String getServiceType() {
        return SosUtil.SERVICE_TYPE;
    }

    /**
     * returns the supported versions of the service
     *
     * @return String[] the supported versions of the service which is connectable by this ServiceAdapter
     */
    @Override
	public String[] getSupportedVersions() {
        return SosUtil.SUPPORTED_VERSIONS;
    }
}
