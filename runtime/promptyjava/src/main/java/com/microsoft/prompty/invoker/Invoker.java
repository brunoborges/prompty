// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.invoker;

import com.microsoft.prompty.Prompty;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for all invokers in the Prompty runtime.
 */
public abstract class Invoker {
    
    protected final Prompty prompty;

    public Invoker(Prompty prompty) {
        this.prompty = prompty;
    }

    /**
     * Invoke the operation synchronously.
     * @param args The input arguments
     * @return The result of the operation
     */
    public abstract Object invoke(Object args);

    /**
     * Invoke the operation asynchronously.
     * @param args The input arguments
     * @return A CompletableFuture containing the result of the operation
     */
    public abstract CompletableFuture<Object> invokeAsync(Object args);

    /**
     * Invoke the operation synchronously with type casting.
     * @param args The input arguments
     * @param <T> The expected return type
     * @return The result cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(Object args, Class<T> type) {
        return (T) invoke(args);
    }

    /**
     * Invoke the operation asynchronously with type casting.
     * @param args The input arguments
     * @param <T> The expected return type
     * @return A CompletableFuture containing the result cast to the specified type
     */
    public <T> CompletableFuture<T> invokeAsync(Object args, Class<T> type) {
        return invokeAsync(args).thenApply(type::cast);
    }
}
