// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.invoker;

import com.microsoft.prompty.Prompty;

import java.util.concurrent.CompletableFuture;

/**
 * Pass-through invoker that does nothing.
 * Implements the no-operation pattern for all invoker types.
 */
public class NoOpInvoker extends Invoker {

    public NoOpInvoker(Prompty prompty) {
        super(prompty);
    }

    @Override
    public Object invoke(Object args) {
        return args;
    }

    @Override
    public CompletableFuture<Object> invokeAsync(Object args) {
        return CompletableFuture.completedFuture(args);
    }
}
