// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import com.microsoft.prompty.model.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Load tests equivalent to C# LoadTests.cs
 */
public class LoadTests {

    @BeforeEach
    public void setUp() {
        // Set environment variable equivalent to C# test setup
        System.setProperty("AZURE_OPENAI_ENDPOINT", "ENDPOINT_VALUE");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "prompty/basic.prompty",
        "prompty/functions.prompty",
        "prompty/chat.prompty"
    })
    public void testLoadRaw(String path) {
        String resourcePath = getClass().getClassLoader().getResource(path).getPath();
        Prompty prompty = Prompty.load(resourcePath);

        assertNotNull(prompty);
        assertNotNull(prompty.getContent());
        assertFalse(prompty.getContent().trim().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "prompty/basic.prompty",
        "prompty/functions.prompty"
    })
    public void testLoadRawWithConfig(String path) {
        String resourcePath = getClass().getClassLoader().getResource(path).getPath();
        Prompty prompty = Prompty.load(resourcePath, "fake");

        assertNotNull(prompty.getModel());
        assertNotNull(prompty.getModel().getConnection());
        assertEquals("FAKE_TYPE", prompty.getModel().getConnection().getType());
    }

    @Test
    public void testBasicSampleParameters() {
        String resourcePath = getClass().getClassLoader().getResource("prompty/basic.prompty").getPath();
        Prompty prompty = Prompty.load(resourcePath);
        
        String[] props = {"firstName", "lastName", "question"};
        String[] samples = {"Jane", "Doe", "What is the meaning of life?"};
        
        for (int i = 0; i < props.length; i++) {
            String prop = props[i];
            assertNotNull(prompty.getInputs());
            assertNotNull(prompty.getInputs().get(prop));
            assertEquals(PropertyType.STRING, prompty.getInputs().get(prop).getType());
            assertEquals(samples[i], prompty.getInputs().get(prop).getSample());
        }
    }

    @Test
    public void testBasicParameters() {
        String resourcePath = getClass().getClassLoader().getResource("prompty/basic_props.prompty").getPath();
        Prompty prompty = Prompty.load(resourcePath);
        
        String[] props = {"firstName", "lastName", "question", "age", "pct", "valid", "items"};
        PropertyType[] types = {
            PropertyType.STRING, PropertyType.STRING, PropertyType.STRING,
            PropertyType.NUMBER, PropertyType.NUMBER, PropertyType.BOOLEAN, PropertyType.ARRAY
        };
        
        String[] vals = {"one", "two", "three"};
        Object[] samples = {"Jane", "Doe", "What is the meaning of life?", 45, 1.9, true, vals};
        Object[] defaults = {"User", null, null, 18, 1.7, false, null};
        
        for (int i = 0; i < props.length; i++) {
            String prop = props[i];
            assertNotNull(prompty.getInputs().get(prop));
            assertEquals(types[i], prompty.getInputs().get(prop).getType());
            
            if (prompty.getInputs().get(prop).getType() == PropertyType.NUMBER) {
                double sTruth = Math.round(((Number) samples[i]).doubleValue() * 100000.0) / 100000.0;
                double dTruth = defaults[i] != null ? 
                    Math.round(((Number) defaults[i]).doubleValue() * 100000.0) / 100000.0 : 0.0;
                
                if (prompty.getInputs().get(prop).getSample() != null) {
                    double sValue = Math.round(((Number) prompty.getInputs().get(prop).getSample()).doubleValue() * 100000.0) / 100000.0;
                    assertEquals(sTruth, sValue, 0.00001);
                }
                if (prompty.getInputs().get(prop).getDefaultValue() != null) {
                    double dValue = Math.round(((Number) prompty.getInputs().get(prop).getDefaultValue()).doubleValue() * 100000.0) / 100000.0;
                    assertEquals(dTruth, dValue, 0.00001);
                }
            } else {
                if (prompty.getInputs().get(prop).getType() == PropertyType.ARRAY) {
                    // Special handling for array types - convert both to lists for comparison
                    Object sample = prompty.getInputs().get(prop).getSample();
                    Object defaultVal = prompty.getInputs().get(prop).getDefaultValue();
                    
                    if (sample != null && samples[i] != null) {
                        assertEquals(java.util.Arrays.asList((String[]) samples[i]), 
                                   sample instanceof java.util.List ? sample : java.util.Arrays.asList((String[]) sample));
                    }
                    if (defaultVal != null && defaults[i] != null) {
                        assertEquals(java.util.Arrays.asList((String[]) defaults[i]), 
                                   defaultVal instanceof java.util.List ? defaultVal : java.util.Arrays.asList((String[]) defaultVal));
                    }
                } else {
                    assertEquals(samples[i], prompty.getInputs().get(prop).getSample());
                    assertEquals(defaults[i], prompty.getInputs().get(prop).getDefaultValue());
                }
            }
            assertEquals("The " + prop + " description", prompty.getInputs().get(prop).getDescription());
        }
    }

    @Test
    public void testBasicPromptyCreation() {
        Prompty prompty = new Prompty();
        assertNotNull(prompty);
        assertEquals("", prompty.getName());
        assertEquals("", prompty.getVersion());
    }

    @Test
    public void testSampleGeneration() {
        Prompty prompty = new Prompty();
        var sample = prompty.getSample();
        assertNotNull(sample);
        assertTrue(sample.isEmpty());
    }

    @Test
    public void testValidateInputs() {
        Prompty prompty = new Prompty();
        var inputs = java.util.Map.of();
        
        var result = prompty.validateInputs(inputs, false);
        assertNotNull(result);
    }

    @Test
    public void testLoadFromContent() {
        String promptyContent = """
            ---
            name: "Test Prompty"
            description: "A test prompty"
            model:
              api: chat
              configuration:
                type: openai
            inputs:
              name:
                type: string
                description: "User name"
            ---
            Hello {{name}}!
            """;
        
        var globalConfig = java.util.Map.<String, Object>of();
        
        Prompty prompty = Prompty.load(promptyContent, globalConfig, null);
        
        assertNotNull(prompty);
        assertEquals("Test Prompty", prompty.getName());
        assertEquals("A test prompty", prompty.getDescription());
        assertTrue(prompty.getContent().contains("Hello {{name}}!"));
        assertNotNull(prompty.getInputs().get("name"));
        assertEquals(PropertyType.STRING, prompty.getInputs().get("name").getType());
    }
}
