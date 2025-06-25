// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.model;

import java.util.List;

/**
 * Metadata information for a Prompty template.
 */
public class Metadata {
    
    /**
     * List of authors of the prompty template.
     */
    private List<String> authors;
    
    /**
     * List of tags associated with the prompty template.
     */
    private List<String> tags;

    // Constructors
    public Metadata() {}

    public Metadata(List<String> authors, List<String> tags) {
        this.authors = authors;
        this.tags = tags;
    }

    // Getters and Setters
    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "authors=" + authors +
                ", tags=" + tags +
                '}';
    }
}
