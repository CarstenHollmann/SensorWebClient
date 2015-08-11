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
package org.n52.shared;

import java.io.Serializable;

import org.n52.io.crs.BoundingBox;

public abstract class Metadata implements Serializable {

	private static final long serialVersionUID = 3858316094546162860L;

	private String serviceUrl; // mandatory

	private String version; // mandatory

	private String title = "NA";

	private String metadataHandler;

	private String adapter;

	private boolean initialized = false;

	private boolean protectedService = false; // default
	
	private boolean hasDonePositionRequest = false;

    private String configuredItemName;

    private String srs;

    private boolean canGeneralize = false; // default

    private boolean eventing = false; // default

    private boolean autoZoom = true; // default

    private int requestChunk = 300; // default

    private int timeout = 10000; // default

    private boolean forceXYAxisOrder = false; // default

    private int httpConnectionPoolSize = 50; // default
    
    private boolean waterML = false; // default
    
    private boolean supportsFirstLatest = false; // default

    private boolean gdaPrefinal = false; // default

    private BoundingBox configuredExtent;

	protected Metadata() {
		// for serialization
	}

	public Metadata(String url, String title, String version) {
		setServiceUrl(url);
		setVersion(version);
		setTitle(title);
	}

	public Metadata(MetadataBuilder builder) {
		this(builder.getServiceURL(), builder.getServiceName(), builder.getServiceVersion());
		this.configuredItemName = builder.getServiceName();
        this.autoZoom = builder.isAutoZoom();
        this.forceXYAxisOrder = builder.isForceXYAxisOrder();
        this.requestChunk = builder.getRequestChunk();
        this.timeout = builder.getTimeout();
        this.configuredExtent = builder.getConfiguredServiceExtent();
        this.protectedService = builder.isProctectedService();
        this.eventing = builder.isEventing();
        this.httpConnectionPoolSize = builder.getHttpConnectionPoolSize();
        this.metadataHandler = builder.getMetadataHandler();
        this.adapter = builder.getAdapter();
        this.gdaPrefinal = builder.isGdaPrefinal();
        this.waterML = builder.isWaterML();
        this.supportsFirstLatest = builder.isSupportsFirstLatest();
	}
	
	public abstract String getDefaultMetadataHandler();

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the configured metadata handler or <code>null</code> when called
	 *         from client side.
	 */
	public String getMetadataHandler() {
		return metadataHandler;
	}

	public final void setMetadataHandler(String handler) {
		// is null when used on client side
		this.metadataHandler = handler != null ? handler.trim() : null;
	}

	/**
	 * @return the configured SOS adapter or <code>null</code> when called from
	 *         client side.
	 */
	public String getAdapter() {
		return adapter;
	}

	public final void setAdapter(String adapter) {
		// is null when used on client side
		this.adapter = adapter != null ? adapter.trim() : null;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Indicates that the metadata has been filled with data requested from
	 * service.
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	public void setProtectedService(boolean protectedService) {
		this.protectedService = protectedService;
	}

	public boolean isProtectedService() {
		return protectedService;
	}
	
	public String getSrs() {
        return this.srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    public String getConfiguredItemName() {
        return configuredItemName;
    }
    
    public boolean hasDonePositionRequest() {
        return this.hasDonePositionRequest;
    }

    public void setHasDonePositionRequest(boolean hasDonePositionRequest) {
        this.hasDonePositionRequest = hasDonePositionRequest;
    }

    public boolean canGeneralize() {
        return canGeneralize;
    }

    public void setCanGeneralize(boolean canGeneralize) {
        this.canGeneralize = canGeneralize;
    }
    
    public boolean isEventing() {
        return eventing;
    }

    public boolean isAutoZoom() {
        return autoZoom;
    }

    public boolean isForceXYAxisOrder() {
        return forceXYAxisOrder;
    }

    public int getHttpConnectionPoolSize() {
        return httpConnectionPoolSize;
    }

    public int getRequestChunk() {
        return requestChunk;
    }

    public int getTimeout() {
        return timeout;
    }
    
    public boolean isWaterML() {
        return waterML;
    }

    public boolean isGdaPrefinal() {
        return gdaPrefinal;
    }
    
    public boolean isSupportsFirstLatest() {
        return supportsFirstLatest;
    }

    /**
     * @return the service's extent.
     */
    public BoundingBox getConfiguredExtent() {
        return configuredExtent;
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" [ ");
		sb.append("parameterId: ").append(serviceUrl).append(", ");
		sb.append("initialized: ").append(initialized).append(", ");
		sb.append("version: ").append(version);
		sb.append(" ]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metadata other = (Metadata) obj;
		if (serviceUrl == null) {
			if (other.serviceUrl != null)
				return false;
		} else if (!serviceUrl.equals(other.serviceUrl))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public String getGlobalId() {
		String[] parameters = new String[] { serviceUrl, version };
		IdGenerator idGenerator = new MD5HashGenerator("srv_");
		return idGenerator.generate(parameters);
	}

	public void clone(Metadata clone) {
		clone.autoZoom = this.autoZoom;
        clone.forceXYAxisOrder = this.forceXYAxisOrder;
        clone.requestChunk = this.requestChunk;
        clone.timeout = this.timeout;
        clone.eventing = this.eventing;
        clone.configuredExtent = this.configuredExtent;
        clone.waterML = this.waterML;
        clone.supportsFirstLatest = this.supportsFirstLatest;
        clone.gdaPrefinal = this.gdaPrefinal;
        clone.setProtectedService(this.isProtectedService());
		clone.setMetadataHandler(this.getMetadataHandler());
        clone.setAdapter(this.getAdapter());
	}

}
