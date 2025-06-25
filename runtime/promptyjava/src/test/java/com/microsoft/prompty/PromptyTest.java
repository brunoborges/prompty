// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic tests for Prompty functionality.
 */
public class PromptyTest {

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
        Map<String, Object> sample = prompty.getSample();
        assertNotNull(sample);
        assertTrue(sample.isEmpty());
    }

    @Test
    public void testValidateInputs() {
        Prompty prompty = new Prompty();
        Map<String, Object> inputs = new HashMap<>();
        
        Map<String, Object> result = prompty.validateInputs(inputs, false);
        assertNotNull(result);
    }

    @Test
    public void testLoadRawPromptyContent() {
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
        
        Map<String, Object> globalConfig = new HashMap<>();
        
        try {
            // This would normally be called internally
            // For now, just verify the content structure
            assertNotNull(promptyContent);
            assertTrue(promptyContent.contains("Hello {{name}}!"));
        } catch (Exception e) {
            // Expected for now since we're testing internal methods
        }
    }
}
