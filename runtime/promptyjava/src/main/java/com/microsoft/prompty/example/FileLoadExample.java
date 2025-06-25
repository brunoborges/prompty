// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.example;

import com.microsoft.prompty.Prompty;
import java.util.Map;
import java.util.HashMap;

/**
 * Example demonstrating loading a real prompty file.
 */
public class FileLoadExample {

    public static void main(String[] args) {
        try {
            // Path to the basic prompty file
            String promptyPath = "/Users/brunoborges/work/prompty/runtime/prompty/tests/prompts/basic.prompty";
            
            // Load the prompty file
            Prompty prompty = Prompty.load(promptyPath);
            
            System.out.println("=== Loaded Prompty File ===");
            System.out.println("Name: " + prompty.getName());
            System.out.println("Description: " + prompty.getDescription());
            System.out.println("Version: " + prompty.getVersion());
            
            // Display model information
            if (prompty.getModel() != null) {
                System.out.println("\n=== Model Configuration ===");
                System.out.println("API: " + prompty.getModel().getApi());
                if (prompty.getModel().getConnection() != null) {
                    System.out.println("Connection Type: " + prompty.getModel().getConnection().getType());
                }
            }
            
            // Display inputs
            System.out.println("\n=== Input Definitions ===");
            prompty.getInputs().forEach((key, input) -> {
                System.out.println("- " + key + " (" + input.getType() + ")");
                System.out.println("  Description: " + input.getDescription());
                if (input.getDefaultValue() != null) {
                    System.out.println("  Default: " + input.getDefaultValue());
                }
                if (input.getSample() != null) {
                    System.out.println("  Sample: " + input.getSample());
                }
                System.out.println("  Required: " + input.isRequired());
            });
            
            // Display sample values
            System.out.println("\n=== Sample Values ===");
            Map<String, Object> samples = prompty.getSample();
            samples.forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
            
            // Try to prepare the template with sample data
            System.out.println("\n=== Template Preparation ===");
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("firstName", "John");
            inputs.put("lastName", "Smith");
            inputs.put("question", "What is the best way to learn Java?");
            inputs.put("query", "Answer programming questions");
            inputs.put("table", "programming");
            
            try {
                Object prepared = prompty.prepare(inputs, false);
                System.out.println("Prepared template result:");
                System.out.println(prepared);
            } catch (Exception e) {
                System.out.println("Template preparation failed: " + e.getMessage());
                // This is expected since we don't have a full executor implementation yet
            }
            
        } catch (Exception e) {
            System.err.println("Error loading prompty file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
