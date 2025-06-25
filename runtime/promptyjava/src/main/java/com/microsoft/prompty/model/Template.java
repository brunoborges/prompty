// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

/**
 * Template configuration for a Prompty file.
 */
public class Template {
    
    /**
     * The default format for templates.
     */
    public static final String DEFAULT_FORMAT = "jinja2";
    
    /**
     * The default parser for templates.
     */
    public static final String DEFAULT_PARSER = "prompty";
    
    /**
     * Gets or sets the format of the template (e.g., "jinja2", "mustache").
     */
    private String format = DEFAULT_FORMAT;
    
    /**
     * Gets or sets the parser type for the template.
     */
    private String parser = DEFAULT_PARSER;

    // Constructors
    public Template() {}

    public Template(String format, String parser) {
        this.format = format != null ? format : DEFAULT_FORMAT;
        this.parser = parser != null ? parser : DEFAULT_PARSER;
    }

    // Getters and Setters
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format != null ? format : DEFAULT_FORMAT;
    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser != null ? parser : DEFAULT_PARSER;
    }

    @Override
    public String toString() {
        return "Template{" +
                "format='" + format + '\'' +
                ", parser='" + parser + '\'' +
                '}';
    }
}
