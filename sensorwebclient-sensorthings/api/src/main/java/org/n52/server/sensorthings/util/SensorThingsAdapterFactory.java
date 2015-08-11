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
package org.n52.server.sensorthings.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.n52.oxf.util.web.GzipEnabledHttpClient;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.ProxyAwareHttpClient;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.n52.server.sensorthings.da.SensorThingsAdapter;
import org.n52.shared.serializable.pojos.sensorthings.SensorThingsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorThingsAdapterFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(SensorThingsAdapterFactory.class);

    /**
     * Creates an adapter to make requests to an SensorThings instance. If the given metadata does not contain a full qualified
     * class name defining the adapter implementation the default one is returned.
     *
     * @param metadata the SensorThings metadata where to create the SensorThings adapter implementation from.
     * @return the custom adapter implementation, or the default {@link SensorThingsAdapter}.
     */
    public static SensorThingsAdapter createSensorThingsAdapter(SensorThingsMetadata metadata) {
        String adapter = metadata.getAdapter();
        String version = metadata.getVersion();
        try {
        	SensorThingsAdapter sensorThingsAdapter = new SensorThingsAdapter(version);
        	sensorThingsAdapter.setHttpClient(createHttpClient(metadata));
            if (adapter == null) {
                return sensorThingsAdapter;
            } else {

                if (!SensorThingsAdapter.class.isAssignableFrom(Class.forName(adapter))) {
                    LOGGER.warn("'{}' is not an SOSAdapter implementation! Create default.", adapter);
                    return sensorThingsAdapter;
                }
                @SuppressWarnings("unchecked") // unassignable case handled already
                Class<SensorThingsAdapter> clazz = (Class<SensorThingsAdapter>) Class.forName(adapter);
                Class< ?>[] arguments = new Class< ?>[]{String.class};
                Constructor<SensorThingsAdapter> constructor = clazz.getConstructor(arguments);
                sensorThingsAdapter = constructor.newInstance(version);
                sensorThingsAdapter.setHttpClient(createHttpClient(metadata));
                return sensorThingsAdapter;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find Adapter class '" + adapter + "'.", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid Adapter constructor for '" + adapter + "'.", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create Adapter for '" + adapter + "'.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Not allowed to create Adapter for '" + adapter + "'.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Instantiation failed for Adapter " + adapter + "'.", e);
        }
    }

    private static HttpClient createHttpClient(SensorThingsMetadata metadata) {
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(metadata.getHttpConnectionPoolSize());

        int timeout = metadata.getTimeout();
        SimpleHttpClient simpleClient = new SimpleHttpClient(timeout, timeout, cm);
        return new GzipEnabledHttpClient(new ProxyAwareHttpClient(simpleClient));
    }
}