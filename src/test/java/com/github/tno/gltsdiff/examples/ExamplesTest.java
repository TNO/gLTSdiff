//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Regression tests for the examples. */
public class ExamplesTest {
    /**
     * Test an example.
     *
     * @param exampleName The name of the example.
     * @throws IOException In case of an I/O error.
     * @throws ReflectiveOperationException In case there is an error in using reflection to run the example.
     */
    @ParameterizedTest
    @MethodSource("testExampleData")
    public void testExample(String exampleName) throws IOException, ReflectiveOperationException {
        // Copy example files to a new directory, as expected output.
        Path exampleActualPath = Paths.get("examples").resolve(exampleName);
        Path exampleExpectedPath = Paths.get("examples_expected").resolve(exampleName);
        deleteDirectory(exampleExpectedPath);
        copyDirectory(exampleActualPath, exampleExpectedPath);

        // Run the example.
        String className = this.getClass().getPackageName() + "." + exampleName + "Example";
        Class<?> exampleClass = this.getClass().getClassLoader().loadClass(className);
        Method mainMethod = exampleClass.getDeclaredMethod("main", String[].class);
        Object methodArg = new String[0];
        mainMethod.invoke(null, methodArg);

        // Compare expected and actual output. Skip rendered SVG files as they may differ for different DOT versions.
        Predicate<Path> skipSvgFiles = path -> path.getFileName().toString().endsWith(".svg");
        checkDirectoriesHaveSameContent(exampleExpectedPath, exampleActualPath, skipSvgFiles);

        // Directories are the same. Clean up.
        deleteDirectory(exampleExpectedPath);
    }

    /**
     * Returns example names to test, for {@link #testExample}.
     *
     * @return Example names.
     * @throws IOException In case of an I/O error.
     */
    private static Stream<Arguments> testExampleData() throws IOException {
        Path examplesPath = Paths.get("examples");
        return Files.list(examplesPath).map(p -> Arguments.of(p.getFileName().toString()));
    }

    /**
     * Delete a directory, including the files it contains, recursively.
     *
     * @param root The root of the directory to delete.
     * @throws IOException In case of an I/O error.
     */
    private static void deleteDirectory(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walk(root).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    /**
     * Copy the contents of a directory to another directory, recursively.
     *
     * @param fromRoot The root of the directory whose contents to copy. Must exist.
     * @param toRoot The root of the directory to which to copy the contents. Is created if it does not yet exist.
     * @throws IOException In case of an I/O error.
     */
    private static void copyDirectory(Path fromRoot, Path toRoot) throws IOException {
        try (Stream<Path> stream = Files.walk(fromRoot)) {
            stream.forEach(from -> {
                Path to = toRoot.resolve(fromRoot.relativize(from));
                try {
                    Files.createDirectories(to.getParent());
                    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy a file.", e);
                }
            });
        }
    }

    /**
     * Check two directories, recursively, to ensure that they have the same content, and fail if they don't.
     *
     * @param directory1 The first directory to compare.
     * @param directory2 The second directory to compare.
     * @param fileSkipFilter The filter to determine for which files to skip comparing their content. If the filter
     *     accepts the file, the content is not compared.
     * @throws IOException In case of an I/O error.
     */
    private void checkDirectoriesHaveSameContent(Path directory1, Path directory2, Predicate<Path> fileSkipFilter)
            throws IOException
    {
        // Check that the directories have the same files.
        List<Path> files1 = Files.find(directory1, Integer.MAX_VALUE, (p, b) -> Files.isRegularFile(p))
                .map(p -> directory1.relativize(p)).collect(Collectors.toList());
        List<Path> files2 = Files.find(directory2, Integer.MAX_VALUE, (p, b) -> Files.isRegularFile(p))
                .map(p -> directory2.relativize(p)).collect(Collectors.toList());
        String filesTxt1 = files1.stream().map(f -> f.toString()).sorted().collect(Collectors.joining("\n"));
        String filesTxt2 = files2.stream().map(f -> f.toString()).sorted().collect(Collectors.joining("\n"));
        assertEquals(filesTxt1, filesTxt2, "Directories do not have same files: " + directory1 + " and " + directory2);

        // Check that the files have the same content.
        for (Path path: files1) {
            if (fileSkipFilter.test(path)) {
                continue;
            }
            Path file1 = directory1.resolve(path);
            Path file2 = directory2.resolve(path);
            String content1 = Files.readString(file1, StandardCharsets.UTF_8);
            String content2 = Files.readString(file2, StandardCharsets.UTF_8);
            assertEquals(content1, content2, "Files do not have the same content: " + file1 + " and " + file2);
        }
    }

    /**
     * Clean up 'examples_expected' directory after the tests.
     *
     * @throws IOException In case of an I/O error.
     */
    @AfterAll
    public static void afterAll() throws IOException {
        Path examplesExpectedPath = Paths.get("examples_expected");
        if (Files.isDirectory(examplesExpectedPath) && Files.list(examplesExpectedPath).count() == 0) {
            Files.delete(examplesExpectedPath);
        }
    }
}
