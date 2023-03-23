//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/** Utilities for rendering DOT files. */
public class DotRenderUtil {
    /** Constructor for the DotRenderUtil class. */
    private DotRenderUtil() {
        // Static class.
    }

    /**
     * Renders a DOT file using 'dot' (part of GraphViz).
     *
     * <p>
     * If the 'DOT_PATH' environment variable is set, the DOT executable path is taken from that variable. Otherwise
     * "dot" is used as executable, and 'dot' must on the 'PATH'.
     * </p>
     *
     * @param dotFilePath The path to the DOT file to render.
     * @note The path for the resulting SVG file is the DOT file path with '.svg' added to it.
     * @throws IOException In case of an I/O error.
     * @throws IllegalStateException If the DOT executable did not successfully terminate.
     */
    public static void renderDot(Path dotFilePath) throws IOException {
        renderDot(dotFilePath, null);
    }

    /**
     * Renders a DOT file using 'dot' (part of GraphViz).
     *
     * <p>
     * If the 'DOT_PATH' environment variable is set, the DOT executable path is taken from that variable. Otherwise
     * "dot" is used as executable, and 'dot' must on the 'PATH'.
     * </p>
     *
     * @param dotFilePath The path to the DOT file to render.
     * @param svgFilePath The path to the SVG file to produce. May be {@code null} to use the DOT file path with '.svg'
     *     added to it.
     * @throws IOException In case of an I/O error.
     * @throws IllegalStateException If the DOT executable did not successfully terminate.
     */
    public static void renderDot(Path dotFilePath, Path svgFilePath) throws IOException {
        // Get DOT executable path.
        String dotExecPath = System.getenv("DOT_PATH");
        if (dotExecPath == null || dotExecPath.trim().isEmpty()) {
            dotExecPath = "dot";
        }

        // Create the process.
        ProcessBuilder builder = new ProcessBuilder();
        List<String> command = new ArrayList<>();
        command.add(dotExecPath);
        command.add("-Tsvg");
        if (svgFilePath == null) {
            command.add("-O");
        } else {
            command.add("-o");
            command.add(svgFilePath.toString());
        }
        command.add(dotFilePath.toString());
        builder.command(command);
        builder.inheritIO();

        // Start the process and wait for it to terminate.
        Process process = builder.start();
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for DOT to finish.", e);
        }

        // Check that DOT terminated successfully.
        Preconditions.checkState(exitCode == 0,
                "DOT execution terminated with non-zero exit code: " + Integer.toString(exitCode));
    }
}
