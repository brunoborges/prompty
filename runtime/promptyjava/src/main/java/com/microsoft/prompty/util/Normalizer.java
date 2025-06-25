// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for normalizing prompty configurations.
 */
public class Normalizer {

    /**
     * Normalize a configuration map with path context.
     * @param config Configuration map to normalize
     * @param path File path for context
     * @return Normalized configuration map
     */
    public static Map<String, Object> normalize(Map<String, Object> config, String path) {
        if (config == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> normalized = new HashMap<>(config);
        
        // Add path-based normalization logic here
        // This would include resolving relative paths, etc.
        if (path != null) {
            Path filePath = Paths.get(path);
            normalized.put("__path__", filePath.toAbsolutePath().toString());
            normalized.put("__directory__", filePath.getParent().toString());
        }
        
        return normalized;
    }

    /**
     * Normalize a configuration map without path context.
     * @param config Configuration map to normalize
     * @return Normalized configuration map
     */
    public static Map<String, Object> normalize(Map<String, Object> config) {
        return normalize(config, null);
    }
}
