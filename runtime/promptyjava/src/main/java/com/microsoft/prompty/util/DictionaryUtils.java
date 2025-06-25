// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for dictionary/map operations.
 */
public class DictionaryUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert an object to a parameter dictionary.
     * @param obj The object to convert
     * @return Map representation of the object
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toParamDictionary(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        
        // Use Jackson to convert object to Map
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            // Fallback to simple toString mapping
            Map<String, Object> result = new HashMap<>();
            result.put("value", obj);
            return result;
        }
    }

    /**
     * Perform parameter hoisting - merge two maps with precedence.
     * @param primary Primary map (takes precedence)
     * @param fallback Fallback map
     * @return Merged map
     */
    public static Map<String, Object> paramHoisting(Map<String, Object> primary, Map<String, Object> fallback) {
        if (fallback == null) {
            return primary != null ? new HashMap<>(primary) : new HashMap<>();
        }
        
        if (primary == null) {
            return new HashMap<>(fallback);
        }
        
        Map<String, Object> result = new HashMap<>(fallback);
        result.putAll(primary);
        return result;
    }

    /**
     * Get a value from a map with a default.
     * @param map The map to search
     * @param key The key to look for
     * @param defaultValue Default value if key not found
     * @param <T> Type of the value
     * @return The value or default
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> map, String key, T defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Get a value from a map.
     * @param map The map to search
     * @param key The key to look for
     * @param <T> Type of the value
     * @return The value or null
     */
    public static <T> T getValue(Map<String, Object> map, String key) {
        return getValue(map, key, null);
    }

    /**
     * Get a nested map from a map.
     * @param map The parent map
     * @param key The key to look for
     * @return The nested map or empty map if not found
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getConfig(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return new HashMap<>();
        }
        
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        
        return new HashMap<>();
    }

    /**
     * Get a list from a map.
     * @param map The map to search
     * @param key The key to look for
     * @param <T> Type of list elements
     * @return The list or empty list if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return new ArrayList<>();
        }
        
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<T>) value;
        }
        
        return new ArrayList<>();
    }

    /**
     * Expand a nested object structure into a flat map.
     * @param obj The object to expand
     * @return Flattened map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> expand(Object obj) {
        Map<String, Object> result = new HashMap<>();
        
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), getValue(entry.getValue()));
            }
        }
        
        return result;
    }

    private static Object getValue(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof Map) {
            return expand(obj);
        }
        
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.stream()
                .filter(Objects::nonNull)
                .map(DictionaryUtils::toParamDictionary)
                .collect(Collectors.toList());
        }
        
        return obj;
    }
}
