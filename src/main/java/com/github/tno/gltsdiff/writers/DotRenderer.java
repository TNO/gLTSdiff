//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.writers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/** Renderer for rendering DOT files. */
public class DotRenderer {
    /** Constructor for the {@link DotRenderer} class. */
    private DotRenderer() {
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
     * <p>
     * The path for the resulting SVG file is the DOT file path with its '.dot' file extension removed (if present), and
     * the '.svg' file extension added to it.
     * </p>
     *
     * @param dotFilePath The path to the DOT file to render.
     * @return The path to the rendered SVG file.
     * @throws IOException In case of an I/O error.
     */
    public static Path renderDot(Path dotFilePath) throws IOException {
        String svgFileName = dotFilePath.getFileName().toString();
        if (svgFileName.endsWith(".dot")) {
            svgFileName = svgFileName.substring(0, svgFileName.length() - ".dot".length());
        }
        svgFileName += ".svg";
        Path svgFilePath = dotFilePath.resolveSibling(svgFileName);
        renderDot(dotFilePath, svgFilePath);
        return svgFilePath;
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
     * @param svgFilePath The path to the SVG file to produce.
     * @throws IOException In case of an I/O error.
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
        command.add("-o");
        command.add(svgFilePath.toString());
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
