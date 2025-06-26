// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.invoker;

import com.microsoft.prompty.Prompty;
import com.microsoft.prompty.renderers.Jinja2Renderer;
import com.microsoft.prompty.parsers.ChatParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Factory for creating invoker instances.
 */
public class InvokerFactory {
    
    private static final InvokerFactory instance = new InvokerFactory();
    
    private final Map<String, BiFunction<String, Prompty, Invoker>> renderers = new HashMap<>();
    private final Map<String, BiFunction<String, Prompty, Invoker>> parsers = new HashMap<>();
    private final Map<String, BiFunction<String, Prompty, Invoker>> executors = new HashMap<>();
    private final Map<String, BiFunction<String, Prompty, Invoker>> processors = new HashMap<>();

    private InvokerFactory() {
        // Register default NOOP invoker
        registerRenderer("NOOP", (type, prompty) -> new NoOpInvoker(prompty));
        registerParser("NOOP", (type, prompty) -> new NoOpInvoker(prompty));
        registerExecutor("NOOP", (type, prompty) -> new NoOpInvoker(prompty));
        registerProcessor("NOOP", (type, prompty) -> new NoOpInvoker(prompty));
        
        // Register Jinja2 renderer
        registerRenderer("jinja2", (type, prompty) -> new Jinja2Renderer(prompty));
        
        // Register parsers
        registerParser("prompty.embedding", (type, prompty) -> new NoOpInvoker(prompty));
        registerParser("prompty.image", (type, prompty) -> new NoOpInvoker(prompty));
        registerParser("prompty.completion", (type, prompty) -> new NoOpInvoker(prompty));
        registerParser("prompty.chat", (type, prompty) -> new ChatParser(prompty));
        registerParser("prompty.openai", (type, prompty) -> new ChatParser(prompty));
    }

    public static InvokerFactory getInstance() {
        return instance;
    }

    /**
     * Create an invoker instance.
     * @param type Invoker type string
     * @param invokerType Type of invoker (RENDERER, PARSER, etc.)
     * @param prompty Associated Prompty instance
     * @return Invoker instance or null if not found
     */
    public Invoker createInvoker(String type, InvokerType invokerType, Prompty prompty) {
        if (type == null) {
            return null;
        }
        
        Map<String, BiFunction<String, Prompty, Invoker>> registry = switch (invokerType) {
            case RENDERER -> renderers;
            case PARSER -> parsers;
            case EXECUTOR -> executors;
            case PROCESSOR -> processors;
        };
        
        BiFunction<String, Prompty, Invoker> factory = registry.get(type);
        if (factory == null) {
            throw new RuntimeException("No invoker registered for type: " + type + " and invoker type: " + invokerType);
        }
        return factory.apply(type, prompty);
    }

    /**
     * Register a renderer factory.
     * @param type Renderer type
     * @param factory Factory function
     */
    public void registerRenderer(String type, BiFunction<String, Prompty, Invoker> factory) {
        renderers.put(type, factory);
    }

    /**
     * Register a parser factory.
     * @param type Parser type
     * @param factory Factory function
     */
    public void registerParser(String type, BiFunction<String, Prompty, Invoker> factory) {
        parsers.put(type, factory);
    }

    /**
     * Register an executor factory.
     * @param type Executor type
     * @param factory Factory function
     */
    public void registerExecutor(String type, BiFunction<String, Prompty, Invoker> factory) {
        executors.put(type, factory);
    }

    /**
     * Register a processor factory.
     * @param type Processor type
     * @param factory Factory function
     */
    public void registerProcessor(String type, BiFunction<String, Prompty, Invoker> factory) {
        processors.put(type, factory);
    }

    /**
     * Register an invoker by type using reflection.
     * @param type Invoker type string
     * @param invokerType Type of invoker (RENDERER, PARSER, etc.)
     * @param invokerClass Class to instantiate
     */
    public void register(String type, InvokerType invokerType, Class<? extends Invoker> invokerClass) {
        BiFunction<String, Prompty, Invoker> factory = (t, p) -> {
            try {
                return invokerClass.getDeclaredConstructor(Prompty.class).newInstance(p);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create invoker instance", e);
            }
        };
        
        switch (invokerType) {
            case RENDERER -> registerRenderer(type, factory);
            case PARSER -> registerParser(type, factory);
            case EXECUTOR -> registerExecutor(type, factory);
            case PROCESSOR -> registerProcessor(type, factory);
        }
    }

    /**
     * Check if an invoker is registered.
     * @param type Invoker type string
     * @param invokerType Type of invoker (RENDERER, PARSER, etc.)
     * @return True if registered, false otherwise
     */
    public boolean isRegistered(String type, InvokerType invokerType) {
        Map<String, BiFunction<String, Prompty, Invoker>> registry = switch (invokerType) {
            case RENDERER -> renderers;
            case PARSER -> parsers;
            case EXECUTOR -> executors;
            case PROCESSOR -> processors;
        };
        
        return registry.containsKey(type);
    }

    /**
     * Get the invoker class for a type.
     * @param type Invoker type string
     * @param invokerType Type of invoker (RENDERER, PARSER, etc.)
     * @return Class or null if not found
     */
    public Class<?> getInvoker(String type, InvokerType invokerType) {
        // For simplicity, return the first match or a default class
        // In a real implementation, this would map to actual classes
        if (isRegistered(type, invokerType)) {
            if ("jinja2".equals(type) && invokerType == InvokerType.RENDERER) {
                return Jinja2Renderer.class;
            }
            if ("prompty.chat".equals(type) && invokerType == InvokerType.PARSER) {
                return ChatParser.class;
            }
            return NoOpInvoker.class;
        }
        return null;
    }
}
