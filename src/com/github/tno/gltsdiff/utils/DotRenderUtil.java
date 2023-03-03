
package com.github.tno.gltsdiff.utils;

import java.io.IOException;
import java.nio.file.Path;

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
     * @throws IOException In case of an I/O error.
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting for the
     *     DOT process to terminate.
     * @throws IllegalStateException If the DOT executable did not successfully terminate.
     */
    public static void renderDot(Path dotFilePath) throws IOException, InterruptedException {
        // Get DOT executable path.
        String dotExecPath = System.getenv("DOT_PATH");
        if (dotExecPath == null || dotExecPath.trim().isEmpty()) {
            dotExecPath = "dot";
        }

        // Create the process.
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(dotExecPath, "-Tsvg", "-O", dotFilePath.toString());
        builder.inheritIO();

        // Start the process and wait for it to terminate.
        Process process = builder.start();
        int exitCode = process.waitFor();

        // Check that DOT terminated successfully.
        Preconditions.checkState(exitCode == 0,
                "DOT execution terminated with non-zero exit code: " + Integer.toString(exitCode));
    }
}
