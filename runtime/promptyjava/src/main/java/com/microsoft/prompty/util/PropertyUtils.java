// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.util;

import com.microsoft.prompty.model.PropertyType;

import java.util.List;
import java.util.Map;

/**
 * Utility class for property type operations.
 */
public class PropertyUtils {

    /**
     * Get property type from a configuration map.
     * @param config Configuration map
     * @return Property type
     */
    public static PropertyType getPropertyType(Map<String, Object> config) {
        if (config == null) {
            return PropertyType.STRING;
        }
        
        String typeStr = (String) config.get("type");
        if (typeStr != null) {
            return PropertyType.fromString(typeStr);
        }
        
        // Try to infer from sample or default
        Object sample = config.get("sample");
        if (sample != null) {
            return getPropertyTypeFromValue(sample);
        }
        
        Object defaultValue = config.get("default");
        if (defaultValue != null) {
            return getPropertyTypeFromValue(defaultValue);
        }
        
        return PropertyType.STRING;
    }

    /**
     * Get property type from a value.
     * @param value The value to analyze
     * @return Inferred property type
     */
    public static PropertyType getPropertyTypeFromValue(Object value) {
        if (value == null) {
            return PropertyType.STRING;
        }
        
        if (value instanceof String) {
            return PropertyType.STRING;
        } else if (value instanceof Number) {
            return PropertyType.NUMBER;
        } else if (value instanceof Boolean) {
            return PropertyType.BOOLEAN;
        } else if (value instanceof List) {
            return PropertyType.ARRAY;
        } else if (value instanceof Map) {
            return PropertyType.OBJECT;
        }
        
        return PropertyType.STRING;
    }

    /**
     * Convert a value to the specified property type.
     * @param type Target property type
     * @param value Value to convert
     * @return Converted value
     */
    public static Object getPropertyValue(PropertyType type, Object value) {
        if (value == null) {
            return null;
        }
        
        return switch (type) {
            case OBJECT -> value;
            case STRING -> value.toString();
            case BOOLEAN -> {
                if (value instanceof Boolean) {
                    yield value;
                }
                String str = value.toString().toLowerCase();
                yield "true".equals(str) || "1".equals(str) || "yes".equals(str);
            }
            case ARRAY -> value;
            case NUMBER -> {
                if (value instanceof Number) {
                    yield value;
                }
                String str = value.toString();
                try {
                    if (str.contains(".")) {
                        yield Double.parseDouble(str);
                    } else {
                        yield Integer.parseInt(str);
                    }
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
            case INTEGER -> {
                if (value instanceof Integer) {
                    yield value;
                }
                if (value instanceof Number) {
                    yield ((Number) value).intValue();
                }
                try {
                    yield Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
        };
    }

    /**
     * Check if a map represents an input definition.
     * @param map The map to check
     * @return true if it looks like an input definition
     */
    public static boolean isInput(Map<String, Object> map) {
        if (map == null) {
            return false;
        }
        
        String[] inputProps = {"type", "default", "sample", "description"};
        for (String prop : inputProps) {
            if (map.containsKey(prop)) {
                return true;
            }
        }
        return false;
    }
}
