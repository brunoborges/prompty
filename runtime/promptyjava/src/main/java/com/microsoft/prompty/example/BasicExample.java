// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.example;

import com.microsoft.prompty.Prompty;
import java.util.Map;
import java.util.HashMap;

/**
 * Example demonstrating basic Prompty usage.
 */
public class BasicExample {

    public static void main(String[] args) {
        // Example 1: Create a simple prompty from text
        String promptyContent = """
            ---
            name: "Greeting Generator"
            description: "Generates personalized greetings"
            model:
              api: chat
              configuration:
                type: openai
            inputs:
              name:
                type: string
                description: "Person's name"
                required: true
              language:
                type: string
                description: "Language for greeting"
                default: "English"
            template:
              format: jinja2
              parser: prompty
            ---
            Generate a friendly greeting for {{name}} in {{language}}.
            """;

        try {
            // Load prompty from content
            Map<String, Object> globalConfig = new HashMap<>();
            Prompty prompty = Prompty.load(promptyContent, globalConfig, null);

            System.out.println("Loaded Prompty: " + prompty.getName());
            System.out.println("Description: " + prompty.getDescription());

            // Prepare the prompty (render template)
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("name", "Alice");
            inputs.put("language", "Spanish");

            Object preparedContent = prompty.prepare(inputs, false);
            System.out.println("Prepared content: " + preparedContent);

            // Get sample inputs
            Map<String, Object> samples = prompty.getSample();
            System.out.println("Sample inputs: " + samples);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
