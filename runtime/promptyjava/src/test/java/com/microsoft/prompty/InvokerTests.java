// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import com.microsoft.prompty.invoker.Invoker;
import com.microsoft.prompty.invoker.InvokerFactory;
import com.microsoft.prompty.invoker.InvokerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Invoker functionality equivalent to C# InvokerTests.cs
 */
public class InvokerTests {

    @BeforeEach
    public void setUp() {
        // Register the fake invoker for testing
        InvokerFactory.getInstance().register("fake", InvokerType.EXECUTOR, FakeInvoker.class);
    }

    @Test
    public void testAutoRegistration() {
        assertTrue(InvokerFactory.getInstance().isRegistered("jinja2", InvokerType.RENDERER));
        assertTrue(InvokerFactory.getInstance().isRegistered("NOOP", InvokerType.RENDERER));
        assertTrue(InvokerFactory.getInstance().isRegistered("NOOP", InvokerType.PARSER));
        assertTrue(InvokerFactory.getInstance().isRegistered("NOOP", InvokerType.EXECUTOR));
        assertTrue(InvokerFactory.getInstance().isRegistered("NOOP", InvokerType.PROCESSOR));
        assertTrue(InvokerFactory.getInstance().isRegistered("prompty.chat", InvokerType.PARSER));
        assertTrue(InvokerFactory.getInstance().isRegistered("fake", InvokerType.EXECUTOR));
    }

    @Test
    public void testCreation() {
        Invoker invoker = InvokerFactory.getInstance().createInvoker("jinja2", InvokerType.RENDERER, new Prompty());
        assertNotNull(invoker);
    }

    @Test
    public void testExecution() {
        Invoker invoker = InvokerFactory.getInstance().createInvoker("fake", InvokerType.EXECUTOR, new Prompty());
        Object result = invoker.invoke("test");
        assertTrue((Boolean) result);

        CompletableFuture<Object> resultAsync = invoker.invokeAsync("test");
        assertTrue((Boolean) resultAsync.join());
    }

    @Test
    public void testMissingInvoker() {
        assertThrows(RuntimeException.class, () -> 
            InvokerFactory.getInstance().createInvoker("missing", InvokerType.EXECUTOR, new Prompty())
        );
    }

    @Test
    public void testMissingInvokerType() {
        assertFalse(InvokerFactory.getInstance().isRegistered("missing", InvokerType.EXECUTOR));
    }

    @Test
    public void testGet() {
        Class<?> invokerType = InvokerFactory.getInstance().getInvoker("jinja2", InvokerType.RENDERER);
        assertNotNull(invokerType);
    }
}
