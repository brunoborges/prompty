// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.renderers;

import com.hubspot.jinjava.Jinjava;
import com.microsoft.prompty.Prompty;
import com.microsoft.prompty.invoker.Invoker;
import com.microsoft.prompty.util.DictionaryUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Jinja2 template renderer using Jinjava library.
 */
public class Jinja2Renderer extends Invoker {

    private final Jinjava jinjava;

    public Jinja2Renderer(Prompty prompty) {
        super(prompty);
        this.jinjava = new Jinjava();
    }

    @Override
    public Object invoke(Object args) {
        if (args == null) {
            return "";
        }

        String template = prompty.getContent();
        if (template == null || template.trim().isEmpty()) {
            return "";
        }

        Map<String, Object> context = DictionaryUtils.toParamDictionary(args);
        
        try {
            return jinjava.render(template, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render Jinja2 template", e);
        }
    }

    @Override
    public CompletableFuture<Object> invokeAsync(Object args) {
        return CompletableFuture.supplyAsync(() -> invoke(args));
    }
}
