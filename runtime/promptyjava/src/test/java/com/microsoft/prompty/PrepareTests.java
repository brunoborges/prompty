// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prepare tests equivalent to C# PrepareTests.cs
 */
public class PrepareTests {

    @BeforeEach
    public void setUp() {
        System.setProperty("AZURE_OPENAI_ENDPOINT", "ENDPOINT_VALUE");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "prompty/basic.prompty",
        "prompty/functions.prompty"
    })
    public void testPrepare(String path) {
        String resourcePath = getClass().getClassLoader().getResource(path).getPath();
        Prompty prompty = Prompty.load(resourcePath);
        Object prepared = prompty.prepare(null, true);
        
        assertNotNull(prepared);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "prompty/basic.prompty",
        "prompty/functions.prompty"
    })
    public void testPrepareWithInput(String path) {
        String replacementText = "OTHER_TEXT_OTHER_TEXT";
        String resourcePath = getClass().getClassLoader().getResource(path).getPath();
        Prompty prompty = Prompty.load(resourcePath);
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("question", replacementText);
        
        Object prepared = prompty.prepare(inputs, true);
        
        assertNotNull(prepared);
        
        // Check if it's a chat format result
        if (prepared instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) prepared;
            if (result.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
                assertFalse(messages.isEmpty());
                
                // Check that our replacement text appears in the content
                boolean found = false;
                for (Map<String, Object> message : messages) {
                    Object content = message.get("content");
                    if (content != null && content.toString().contains(replacementText)) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "Replacement text should appear in prepared content");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "prompty/basic.prompty",
        "prompty/functions.prompty"
    })
    public void testPrepareAsync(String path) {
        String resourcePath = getClass().getClassLoader().getResource(path).getPath();
        Prompty prompty = Prompty.load(resourcePath);
        
        // Test async preparation
        var future = prompty.prepareAsync(null, true);
        Object prepared = future.join();
        
        assertNotNull(prepared);
    }

    static class MyObject {
        public String question = "";
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "prompty/basic.prompty"
    })
    public void testPrepareWithObject(String path) {
        String resourcePath = getClass().getClassLoader().getResource(path).getPath();
        Prompty prompty = Prompty.load(resourcePath);
        
        MyObject obj = new MyObject();
        obj.setQuestion("What is the meaning of everything?");
        
        Object prepared = prompty.prepare(obj, true);
        
        assertNotNull(prepared);
        
        // Verify the object's question appears in the prepared content
        if (prepared instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) prepared;
            if (result.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
                assertFalse(messages.isEmpty());
                
                boolean found = false;
                for (Map<String, Object> message : messages) {
                    Object content = message.get("content");
                    if (content != null && content.toString().contains("meaning of everything")) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "Object property should appear in prepared content");
            }
        }
    }
}
