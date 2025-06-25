// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tool that can be used by a Prompty template.
 */
public class Tool {
    
    /**
     * Gets or sets the unique identifier of the tool.
     */
    private String id;
    
    /**
     * Gets or sets the description of the tool.
     */
    private String description;
    
    /**
     * Gets or sets the type of the tool.
     */
    private String type;
    
    /**
     * Gets or sets the options for the tool.
     */
    private Map<String, Object> options = new HashMap<>();

    // Constructors
    public Tool() {}

    public Tool(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public Tool(String id, String description, String type) {
        this.id = id;
        this.description = description;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options != null ? options : new HashMap<>();
    }

    @Override
    public String toString() {
        return "Tool{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", options=" + options +
                '}';
    }
}
