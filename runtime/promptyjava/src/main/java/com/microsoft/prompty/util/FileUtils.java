// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for file operations.
 */
public class FileUtils {

    /**
     * Read all text from a file.
     * @param path File path
     * @return File content as string
     * @throws IOException if file cannot be read
     */
    public static String readAllText(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    /**
     * Read all text from a file asynchronously.
     * @param path File path
     * @return CompletableFuture with file content
     */
    public static CompletableFuture<String> readAllTextAsync(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return readAllText(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + path, e);
            }
        });
    }

    /**
     * Check if a file exists.
     * @param path File path
     * @return true if file exists, false otherwise
     */
    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    /**
     * Get the directory of a file path.
     * @param path File path
     * @return Directory path
     */
    public static String getDirectory(String path) {
        Path p = Paths.get(path);
        Path parent = p.getParent();
        return parent != null ? parent.toString() : "";
    }

    /**
     * Get the absolute path of a file.
     * @param path File path
     * @return Absolute path
     */
    public static String getAbsolutePath(String path) {
        return Paths.get(path).toAbsolutePath().toString();
    }
}
