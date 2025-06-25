package com.microsoft.prompty.model;

/**
 * Represents the type of a property in a Prompty template.
 */
public enum PropertyType {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    OBJECT("object");

    private final String value;

    PropertyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PropertyType fromString(String text) {
        for (PropertyType type : PropertyType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return STRING; // default
    }

    @Override
    public String toString() {
        return value;
    }
}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
