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
package org.n52.server.mgmt;

import static org.n52.shared.Constants.DEFAULT_SOS_VERSION;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContext;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.n52.server.da.MetadataHandler;
import org.n52.server.da.oxf.DefaultMetadataHandler;
import org.n52.server.util.Statistics;
import org.n52.shared.Metadata;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

public class ConfigurationContext implements ServletContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationContext.class);

    @Autowired
    private ServletContext servletContext;




    private static Map<String, Metadata> serviceMetadatas = Collections.synchronizedMap(new HashMap<String, Metadata>());

    public static int STARTUP_DELAY;

    public static String COPYRIGHT;

    public static String XSL_DIR;

    public static String CACHE_DIR;

    public static String GEN_DIR;

    public static String GEN_URL;

    public static String ZIP_POSTFIX;

    public static String IMAGE_SERVICE;

    public static String GEN_DIR_ZIP;

    public static boolean USE_DEVEL_CACHING = false;

    public static boolean IS_DEV_MODE = true;

    public static final int FILE_KEEPING_TIME = 30 * 60 * 1000;

    public static long SERVER_TIMEOUT = 60 * 1000;

    public static int THREAD_POOL_SIZE = 10;

    public static boolean UPDATE_TASK_RUNNING = false;

    public static boolean FACADE_COMPRESSION = false;

    public static int STATISTICS_INTERVAL = 60;

    public static int TOOLTIP_MIN_COUNT = 50;

    public static List<String> NO_DATA_VALUES;

    public ConfigurationContext() {
        LOGGER.debug("Create ConfigurationContext ...");
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    void init() {
        LOGGER.debug("Initialize ConfigurationContext ...");
        String webappDirectory = servletContext.getRealPath("/");
        webappDirectory += webappDirectory.endsWith("/") ? "" : File.separator;
        parsePreConfiguredServices(webappDirectory + File.separator + "ds");
        XSL_DIR = webappDirectory + File.separator + "xslt" + File.separator;
        CACHE_DIR = webappDirectory + "cache" + File.separator;
        GEN_DIR = webappDirectory + "generated" + File.separator;
        IMAGE_SERVICE = getMandatoryParameterValue("IMAGE_SERVICE");
        ZIP_POSTFIX = getMandatoryParameterValue("ZIP_POSTFIX");
        GEN_URL = getMandatoryParameterValue("GENERATE_URL");
        COPYRIGHT = getMandatoryParameterValue("COPYRIGHT");
        try {
            // parameters which have to be parsed
            THREAD_POOL_SIZE = new Integer(getMandatoryParameterValue("THREAD_POOL_SIZE")).intValue();
            SERVER_TIMEOUT = new Long(getMandatoryParameterValue("SERVER_TIMEOUT")).longValue();
            IS_DEV_MODE = new Boolean(getMandatoryParameterValue("DEV_MODE")).booleanValue();
            FACADE_COMPRESSION = new Boolean(getMandatoryParameterValue("FACADE_COMPRESSION")).booleanValue();
            STATISTICS_INTERVAL = new Integer(getMandatoryParameterValue("STATISTICS_INTERVAL")).intValue();
            STARTUP_DELAY = new Integer(getMandatoryParameterValue("STARTUP_DELAY")).intValue();
            TOOLTIP_MIN_COUNT = new Integer(getMandatoryParameterValue("TOOLTIP_MIN_COUNT")).intValue();
            NO_DATA_VALUES = getNoDataValues(getMandatoryParameterValue("NO_DATA_VALUES"));
        }
        catch (Exception e) {
            LOGGER.error("Could not read context parameter", e);
        }

        GEN_DIR_ZIP = GEN_DIR + "/zipped";
        // USE_DEVEL_CACHING = IS_DEV_MODE;

        LOGGER.info("INITIALIZED SERVER APPLICATION SUCESSFULLY");
        if (IS_DEV_MODE) {
            LOGGER.info("GOING INTO DEV MODE!");
        }
        Statistics.scheduleStatisticsLog(STATISTICS_INTERVAL);
    }

    /**
     * Destroy method called by Spring's lifecycle.
     */
    public void shutdown() {
        Statistics.shutdown();
    }

    private void parsePreConfiguredServices(String dsDirectory) {
        try {
            String path = File.separator + dsDirectory + File.separator;
            File sosDataSource = new File(path + "sos-instances.data.xml");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(sosDataSource, new SosInstanceContentHandler());
        }
        catch (Exception e) {
            LOGGER.error("Could not parse preconfigured sos instances.", e);
        }
    }

    /**
     * Gets the value of the passed in parameter which is considered to be mandatory. If a parameter is
     * missing an exception is being thrown. If the parameter was found but empty a WARN message is logged.
     *
     * @param parameter
     *        the name of the parameter
     * @return the resolved parameter value
     * @throws IllegalStateException
     *         if no parameter (null or empty) could be resolved.
     */
    private String getMandatoryParameterValue(String parameter) {
        String value = servletContext.getInitParameter(parameter);
        if (value == null) {
            throw new IllegalStateException("Parameter '" + parameter + "' was invalid!");
        }
        if (value.isEmpty()) {
            LOGGER.warn("Empty parameter value for parameter {}.", parameter);
        }
        LOGGER.info("Set mandatory parameter {}={}.", parameter, value.trim());
        return value.trim();
    }

    /**
     * Gets the value of the passed in parameter which is considered to be optional. Logs an INFO message if
     * parameter was not set, i.e. that a default value is being used instead. Logs a WARN message if
     * parameter is empty.
     *
     * @param parameter
     *        an optional parameter value.
     * @return the parameter value or <code>null</code> if parameter was not defined.
     */
    public String getOptionalParameterValue(String parameter) {
        String value = servletContext.getInitParameter(parameter);
        if (value == null) {
            LOGGER.info("Using default of parameter {}.", parameter);
        }
        if (value.isEmpty()) {
            LOGGER.warn("Empty parameter value for parameter {}.", parameter);
        }
        LOGGER.info("Set optional parameter {}={}.", parameter, value.trim());
        return value;
    }

    private List<String> getNoDataValues(String noDatas) {
        List<String> values = new ArrayList<String>();
        if (noDatas.length() > 1) {
            String[] seperatedNoDatas = noDatas.split(",");
            for (String sepNoData : seperatedNoDatas) {
                try {
                    sepNoData = sepNoData.trim()
                            .replaceAll("\n", "")
                            .replaceAll("\t", "")
                            .replaceAll(" ", "");
                    values.add(sepNoData);
                }
                catch (NumberFormatException e) {
                    LOGGER.error("NumberFormatException in the NoDataValues");
                }
            }
        }
        return values;
    }

    
    public synchronized static Map<String, Metadata> getServiceMetadatas() {
        return serviceMetadatas;
    }

    public synchronized static Collection<SOSMetadata> getSOSMetadatas() {
        List<SOSMetadata> sosMetadatas = new ArrayList<SOSMetadata>();
        for (Metadata metadata : serviceMetadatas.values()) {
        	if (metadata instanceof SOSMetadata) {
        		sosMetadatas.add((SOSMetadata)metadata);
        	}
        }
        return sosMetadatas;
    }
    
    public synchronized static Collection<Metadata> getMetadatas() {
        List<Metadata> metadatas = new ArrayList<Metadata>();
        for (Metadata metadata : serviceMetadatas.values()) {
        	metadatas.add(metadata);
        }
        return metadatas;
    }
    
    public synchronized static Metadata getMetadata(String url) {
        url = url.trim();
        if (isMetadataAvailable(url)) {
            return getServiceMetadatas().get(url);
        }
        try {
            if (containsServiceMetadata(url)) {
                Metadata metadata = getServiceMetadatas().get(url);
                MetadataHandler handler = createMetadataHandler(metadata);
                handler.performMetadataCompletion();
                if ( !metadata.hasDonePositionRequest()) {
                    MetadataUpdate.updateService(url);
                }
                return metadata;
            } else {
                // try to get metadata with default SOS version.
                SOSMetadata metadata = new SOSMetadata(url, url, DEFAULT_SOS_VERSION);
                MetadataUpdate.updateService(url);
                serviceMetadatas.put(url, metadata);
                return metadata;
            }
        }
        catch (Exception e) {
            // throw new RuntimeException("Error building server metadata", e);
            LOGGER.error("Error building server metadata", e);
            return null; // TODO do not return null
        }
    }

    public synchronized static SOSMetadata getSOSMetadata(String url) {
        url = url.trim();
        if (isMetadataAvailable(url)) {
            return (SOSMetadata) getServiceMetadatas().get(url);
        }
        try {
            if (containsSosMetadata(url)) {
                SOSMetadata metadata = (SOSMetadata) getServiceMetadatas().get(url);
                MetadataHandler handler = createSosMetadataHandler(metadata);
                handler.performMetadataCompletion();
                if ( !metadata.hasDonePositionRequest()) {
                    MetadataUpdate.updateService(url);
                }
                return metadata;
            } else {
                // try to get metadata with default SOS version.
                SOSMetadata metadata = new SOSMetadata(url, url, DEFAULT_SOS_VERSION);
                MetadataUpdate.updateService(url);
                serviceMetadatas.put(url, metadata);
                return metadata;
            }
        }
        catch (Exception e) {
            // throw new RuntimeException("Error building server metadata", e);
            LOGGER.error("Error building server metadata", e);
            return null; // TODO do not return null
        }
    }

    @SuppressWarnings("unchecked")
    public static MetadataHandler createSosMetadataHandler(SOSMetadata metadata) {
        String handler = metadata.getSosMetadataHandler();
        if (handler == null) {
            LOGGER.info("Using default SOS metadata handler for '{}'", metadata.getServiceUrl());
            handler = metadata.getDefaultMetadataHandler();
//            return new DefaultMetadataHandler(metadata);
        }
//        else {
            try {
                Class<MetadataHandler> clazz = (Class<MetadataHandler>) Class.forName(handler);
                Constructor<MetadataHandler> constructor = clazz.getConstructor(metadata.getClass());
                return constructor.newInstance(metadata);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find metadata handler class.", e);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException("Invalid metadata handler constructor. ", e);
            }
            catch (InstantiationException e) {
                throw new RuntimeException("Could not create metadata handler.", e);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException("Not allowed to create metadata handler.", e);
            }
            catch (InvocationTargetException e) {
                throw new RuntimeException("Instantiation of metadata handler failed.", e);
            }
//        }
    }

    @SuppressWarnings("unchecked")
    public static MetadataHandler createMetadataHandler(Metadata metadata) {
    	 String handler = metadata.getMetadataHandler();
         if (handler == null) {
             LOGGER.info("Using default metadata handler for '{}'", metadata.getServiceUrl());
             handler = metadata.getDefaultMetadataHandler();
         }
//         else {
             try {
                 Class<? extends MetadataHandler> clazz = (Class<? extends MetadataHandler>) Class.forName(handler);
                 Constructor<? extends MetadataHandler> constructor = clazz.getConstructor(metadata.getClass());
                 return constructor.newInstance(metadata);
             }
             catch (ClassNotFoundException e) {
                 throw new RuntimeException(String.format("Could not find metadata handler class '%s'.", handler), e);
             }
             catch (NoSuchMethodException e) {
                 throw new RuntimeException("Invalid metadata handler constructor. ", e);
             }
             catch (InstantiationException e) {
                 throw new RuntimeException("Could not create metadata handler.", e);
             }
             catch (IllegalAccessException e) {
                 throw new RuntimeException("Not allowed to create metadata handler.", e);
             }
             catch (InvocationTargetException e) {
                 throw new RuntimeException("Instantiation of metadata handler failed.", e);
             }
//         }
	}

	public static boolean isMetadataAvailable(String sosURL) {
        return containsServiceMetadata(sosURL) && serviceMetadatas.get(sosURL).isInitialized();
    }

    public static boolean containsServiceMetadata(String sosURL) {
        return serviceMetadatas.containsKey(sosURL) && serviceMetadatas.get(sosURL) != null;
    }
    
    public static boolean containsSosMetadata(String sosURL) {
        return serviceMetadatas.containsKey(sosURL) && serviceMetadatas.get(sosURL) != null && serviceMetadatas.get(sosURL) instanceof SOSMetadata;
    }

    public static boolean containsServiceInstance(String instance) {
        return getMetadataForItemName(instance) != null;
    }
    
    /**
     * @param itemName
     *        the configured item name of the SOS.
     * @return the associated {@link SOSMetadata} or <code>null</code> if not found.
     */
    public static Metadata getMetadataForItemName(String itemName) {
        for (Metadata metadata : getMetadatas()) {
            if (metadata.getConfiguredItemName().equals(itemName)) {
                return metadata;
            }
        }
        return null;
    }

    /**
     * @param itemName
     *        the configured item name of the SOS.
     * @return the associated {@link SOSMetadata} or <code>null</code> if not found.
     */
    public static SOSMetadata getSOSMetadataForItemName(String itemName) {
        for (SOSMetadata metadata : getSOSMetadatas()) {
            if (metadata.getConfiguredItemName().equals(itemName)) {
                return metadata;
            }
        }
        return null;
    }

    private static String getVersion(String sosURL) throws TimeoutException, Exception {
        Metadata serviceMetadata = serviceMetadatas.get(sosURL);
        if (serviceMetadata != null) {
            return serviceMetadata.getVersion();
        }
        else {
            return getMetadata(sosURL).getVersion();
        }
    }

    public static void initializeMetadata(Metadata metadata) {
        Metadata old = serviceMetadatas.put(metadata.getServiceUrl(), metadata);
        LOGGER.debug(old == null ? "metadata initialized." : "metadata replaced with " + metadata);
        metadata.setInitialized(true);
    }
    
    public static void addNewMetadata(Metadata metadata) {
        String serviceURL = metadata.getServiceUrl();
        LOGGER.debug(String.format("Add new metadata for '%s' ", serviceURL));
        serviceMetadatas.put(serviceURL, metadata);
    }

    public static Map<String, Metadata> updateMetadata() {
        LOGGER.debug("Update protected services");
        Map<String, Metadata> updatedMetadatas = new HashMap<String, Metadata>();
        for (String metadataKey : serviceMetadatas.keySet()) {
            Metadata metadata = serviceMetadatas.get(metadataKey);
            if (metadata.isProtectedService()) {
                try {
                    MetadataHandler metadataHandler = ConfigurationContext.createMetadataHandler(metadata);
                    Metadata updatedMetadata = metadataHandler.updateMetadata(metadata);
                    updatedMetadatas.put(updatedMetadata.getServiceUrl(), updatedMetadata);
                    LOGGER.debug("Update metadata for service with url '{}'", updatedMetadata.getServiceUrl());
                }
                catch (Exception e) {
                    LOGGER.error("Could not update {} ", metadata, e);
                }
            }
        }
        LOGGER.debug("Update #{} protected services", updatedMetadatas.size());
        return updatedMetadatas;
    }

    public static Map<String, SOSMetadata> updateSOSMetadata() {
        LOGGER.debug("Update protected services");
        Map<String, SOSMetadata> updatedMetadatas = new HashMap<String, SOSMetadata>();
        for (String metadataKey : serviceMetadatas.keySet()) {
        	if (serviceMetadatas.get(metadataKey) instanceof SOSMetadata) {
	            SOSMetadata sosMetadata = (SOSMetadata)serviceMetadatas.get(metadataKey);
	            if (sosMetadata.isProtectedService()) {
	                try {
	                    MetadataHandler metadataHandler = ConfigurationContext.createSosMetadataHandler(sosMetadata);
	                    SOSMetadata updatedMetadata = (SOSMetadata)metadataHandler.updateMetadata(sosMetadata);
	                    updatedMetadatas.put(updatedMetadata.getServiceUrl(), updatedMetadata);
	                    LOGGER.debug("Update metadata for service with url '{}'", updatedMetadata.getServiceUrl());
	                }
	                catch (Exception e) {
	                    LOGGER.error("Could not update {} ", sosMetadata, e);
	                }
	            }
        	}
        }
        LOGGER.debug("Update #{} protected services", updatedMetadatas.size());
        return updatedMetadatas;
    }
}
