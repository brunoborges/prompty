// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import com.microsoft.prompty.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Global configuration management for Prompty.
 */
public class GlobalConfig {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final String[] CONFIG_FILES = {"prompty.json", "prompty.yaml"};

    /**
     * Load global configuration from a directory.
     * @param directory Directory to search for configuration
     * @param configuration Configuration name
     * @return Configuration map
     */
    public static Map<String, Object> load(String directory, String configuration) {
        try {
            Path configPath = null;
            ObjectMapper mapper = null;
            
            // Try to find configuration file in order of preference
            for (String configFile : CONFIG_FILES) {
                Path testPath = Paths.get(directory, configFile);
                if (Files.exists(testPath)) {
                    configPath = testPath;
                    mapper = configFile.endsWith(".json") ? jsonMapper : yamlMapper;
                    break;
                }
            }
            
            if (configPath == null || mapper == null) {
                return new HashMap<>();
            }
            
            String content = FileUtils.readAllText(configPath.toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> configs = mapper.readValue(content, Map.class);
            
            if (configs.containsKey(configuration)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> specificConfig = (Map<String, Object>) configs.get(configuration);
                return specificConfig != null ? specificConfig : new HashMap<>();
            }
            
            return new HashMap<>();
            
        } catch (IOException e) {
            // Log warning and return empty config
            System.err.println("Warning: Failed to load global configuration: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Load global configuration asynchronously.
     * @param directory Directory to search for configuration
     * @param configuration Configuration name
     * @return CompletableFuture with configuration map
     */
    public static CompletableFuture<Map<String, Object>> loadAsync(String directory, String configuration) {
        return CompletableFuture.supplyAsync(() -> load(directory, configuration));
    }
}
