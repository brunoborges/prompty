// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the connection for a model.
 */
public class Connection {
    
    /**
     * The type of the model connection.
     * Used to identify the type of deployment e.g., azure_openai, openai, ...
     * This type will also be used for connection hosting.
     */
    private String type;
    
    /**
     * Gets or sets the Service ID of the model connection.
     */
    private String serviceId;
    
    /**
     * Extra properties that may be included in the serialized model connection.
     * Used to store model specific connection e.g., the deployment name, endpoint, etc.
     */
    private Map<String, Object> extensionData = new HashMap<>();

    // Constructors
    public Connection() {}

    public Connection(String type, String serviceId) {
        this.type = type;
        this.serviceId = serviceId;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensionData() {
        return extensionData;
    }

    @JsonAnySetter
    public void setExtensionData(String key, Object value) {
        if (this.extensionData == null) {
            this.extensionData = new HashMap<>();
        }
        this.extensionData.put(key, value);
    }

    public void setExtensionData(Map<String, Object> extensionData) {
        this.extensionData = extensionData != null ? extensionData : new HashMap<>();
    }

    @Override
    public String toString() {
        return "Connection{" +
                "type='" + type + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", extensionData=" + extensionData +
                '}';
    }
}
