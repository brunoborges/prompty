// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

/**
 * Represents an output for a Prompty file.
 */
public class Output {
    
    /**
     * Gets or sets the type of the output.
     */
    private PropertyType type;
    
    /**
     * Gets or sets the name of the output.
     */
    private String name;
    
    /**
     * Gets or sets a description of the output.
     */
    private String description;
    
    /**
     * Gets or sets JSON Schema describing this output.
     */
    private Object jsonSchema;

    // Constructors
    public Output() {}

    public Output(String name) {
        this.name = name;
    }

    public Output(String name, PropertyType type) {
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(Object jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    @Override
    public String toString() {
        return "Output{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", jsonSchema=" + jsonSchema +
                '}';
    }
}
