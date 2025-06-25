// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import com.microsoft.prompty.invoker.Invoker;
import java.util.concurrent.CompletableFuture;

/**
 * Test class for fake executor used in tests
 */
public class FakeInvoker extends Invoker {
    public FakeInvoker(Prompty prompty) {
        super(prompty);
    }

    @Override
    public Object invoke(Object args) {
        return true;
    }

    @Override
    public CompletableFuture<Object> invokeAsync(Object args) {
        return CompletableFuture.completedFuture(true);
    }
}
