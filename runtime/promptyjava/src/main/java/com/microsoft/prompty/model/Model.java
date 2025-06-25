// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the model to be used by a Prompty file.
 */
public class Model {
    
    /**
     * The default API type.
     */
    public static final String DEFAULT_API = "chat";
    
    /**
     * Gets or sets the unique identifier of the model.
     * This is typically a short string, but can be any string that is compatible with the Prompty file.
     * Typically, depending on the provider, this can replace the entire connection settings if
     * the provider has a way to resolve the model connection from the id.
     */
    private String id;
    
    /**
     * Gets or sets the type of API used by the Prompty file.
     * This is typically a chat or completion API, but can be any API that is compatible with the Prompty file.
     */
    private String api = DEFAULT_API;
    
    /**
     * Gets or sets the options used by the Prompty file.
     * This is typically a set of options that are compatible with the API and connection used by the Prompty file.
     * This optional section is used to specify the options to be used when executing the Prompty file.
     * If this section is not included, the runtime will use the default options for the API and connection used by the Prompty file.
     */
    private Map<String, Object> options = new HashMap<>();
    
    /**
     * Gets or sets the connection used by the Prompty file.
     */
    private Connection connection;

    // Constructors
    public Model() {}

    public Model(String id) {
        this.id = id;
    }

    public Model(String id, String api) {
        this.id = id;
        this.api = api != null && !api.trim().isEmpty() ? api : DEFAULT_API;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        if (api == null || api.trim().isEmpty()) {
            throw new IllegalArgumentException("API cannot be null or empty.");
        }
        this.api = api;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options != null ? options : new HashMap<>();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id='" + id + '\'' +
                ", api='" + api + '\'' +
                ", options=" + options +
                ", connection=" + connection +
                '}';
    }
}
