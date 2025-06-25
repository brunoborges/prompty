// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

/**
 * Represents an input for a Prompty file.
 */
public class Input {
    
    /**
     * Gets or sets the type of the input.
     */
    private PropertyType type;
    
    /**
     * Gets or sets the name of the input.
     */
    private String name;
    
    /**
     * Gets or sets a description of the input.
     */
    private String description;
    
    /**
     * Gets or sets a default value for the input.
     */
    private Object defaultValue;
    
    /**
     * Gets or sets whether the input is considered required (rather than optional).
     * The default is true.
     */
    private boolean required = true;
    
    /**
     * Gets or sets JSON Schema describing this input.
     */
    private Object jsonSchema;
    
    /**
     * Gets or sets a value indicating whether to handle the input value as potential dangerous content.
     * The default is true.
     * When set to false the value of the input is treated as safe content.
     */
    private boolean strict = true;
    
    /**
     * Gets or sets a sample value for the input.
     */
    private Object sample;

    // Constructors
    public Input() {}

    public Input(String name) {
        this.name = name;
    }

    public Input(String name, PropertyType type) {
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

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(Object jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        this.sample = sample;
    }

    @Override
    public String toString() {
        return "Input{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue=" + defaultValue +
                ", required=" + required +
                ", jsonSchema=" + jsonSchema +
                ", strict=" + strict +
                ", sample=" + sample +
                '}';
    }
}
