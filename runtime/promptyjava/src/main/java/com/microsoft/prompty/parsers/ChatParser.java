// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.parsers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.prompty.Prompty;
import com.microsoft.prompty.invoker.Invoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Chat parser for prompty templates.
 * Converts rendered content into a structured chat format.
 */
public class ChatParser extends Invoker {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ChatParser(Prompty prompty) {
        super(prompty);
    }

    @Override
    public Object invoke(Object args) {
        if (args == null) {
            return createEmptyChat();
        }

        String content = args.toString().trim();
        if (content.isEmpty()) {
            return createEmptyChat();
        }

        try {
            // Try to parse as JSON first
            if (content.startsWith("{") || content.startsWith("[")) {
                return objectMapper.readValue(content, new TypeReference<Object>() {});
            }

            // Otherwise, treat as simple text and create a user message
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", content);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", List.of(message));
            
            return result;

        } catch (Exception e) {
            // Fallback to simple text format
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", content);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", List.of(message));
            
            return result;
        }
    }

    @Override
    public CompletableFuture<Object> invokeAsync(Object args) {
        return CompletableFuture.supplyAsync(() -> invoke(args));
    }

    private Map<String, Object> createEmptyChat() {
        Map<String, Object> result = new HashMap<>();
        result.put("messages", List.of());
        return result;
    }
}
