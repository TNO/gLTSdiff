//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.projectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tno.gltsdiff.glts.DiffAutomatonStateProperty;
import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.google.common.collect.ImmutableSet;

/** {@link Projector} tests. */
public class ProjectorsTest {
    /** Test {@link IdentityProjector}. */
    @Test
    public void testIdentityProjector() {
        Projector<String, DiffKind> projector = new IdentityProjector<>();
        assertEquals(Optional.of("e"), projector.project("e", DiffKind.ADDED));
    }

    /**
     * Test {@link DiffKindProjector}.
     *
     * @param expected Expected test output.
     * @param property Test property.
     * @param along The value to project along.
     */
    @ParameterizedTest
    @MethodSource("testDiffKindProjectorData")
    public void testDiffKindProjector(Optional<DiffKind> expected, DiffKind property, DiffKind along) {
        Projector<DiffKind, DiffKind> projector = new DiffKindProjector();
        assertEquals(expected, projector.project(property, along));
    }

    /**
     * Returns test input, value to project along and expected output for {@link #testDiffKindProjector}.
     *
     * @return Test input, value to project along and expected output.
     */
    private static Stream<Arguments> testDiffKindProjectorData() {
        return Stream.of(
                // Projecting 'ADDED' along 'ADDED' should leave 'ADDED'.
                Arguments.of(Optional.of(DiffKind.ADDED), DiffKind.ADDED, DiffKind.ADDED),

                // Projecting 'UNCHANGED' along 'REMOVED' should leave 'REMOVED'.
                Arguments.of(Optional.of(DiffKind.REMOVED), DiffKind.UNCHANGED, DiffKind.REMOVED),

                // Projecting 'ADDED' along 'REMOVED' should leave nothing.
                Arguments.of(Optional.empty(), DiffKind.ADDED, DiffKind.REMOVED),

                // Projecting 'REMOVED' along 'UNCHANGED' should leave nothing.
                Arguments.of(Optional.empty(), DiffKind.REMOVED, DiffKind.UNCHANGED));
    }

    /**
     * Test {@link DiffPropertyProjector} on right argument.
     *
     * @param <T> The type of properties.
     * @param expected Expected test output.
     * @param property Test property.
     * @param along The value to project along.
     */
    @ParameterizedTest
    @MethodSource("testDiffPropertyProjectorOnRightArgumentData")
    public <T> void testDiffPropertyProjectorOnRightArgument(Optional<DiffProperty<T>> expected,
            DiffProperty<T> property, DiffKind along)
    {
        Projector<DiffProperty<T>, DiffKind> projector = new DiffPropertyProjector<>(new IdentityProjector<>(),
                new DiffKindProjector());
        assertEquals(expected, projector.project(property, along));
    }

    /**
     * Returns test input, value to project along and expected output for
     * {@link #testDiffPropertyProjectorOnRightArgument}.
     *
     * @return Test input, value to project along and expected output.
     */
    private static Stream<Arguments> testDiffPropertyProjectorOnRightArgumentData() {
        return Stream.of(
                // Projecting an 'UNCHANGED' difference property along 'REMOVED' should leave a 'REMOVED' one.
                Arguments.of(Optional.of(new DiffProperty<>("e", DiffKind.REMOVED)),
                        new DiffProperty<>("e", DiffKind.UNCHANGED), DiffKind.REMOVED),

                // Projecting an 'ADDED' difference property along 'REMOVED' should leave nothing.
                Arguments.of(Optional.empty(), new DiffProperty<>("e", DiffKind.ADDED), DiffKind.REMOVED));
    }

    /**
     * Test {@link DiffPropertyProjector} on both arguments.
     *
     * @param expected Expected test output.
     * @param property Test property.
     * @param along The value to project along.
     */
    @ParameterizedTest
    @MethodSource("testDiffPropertyProjectorOnBothArgumentsData")
    public void testDiffPropertyProjectorOnBothArguments(Optional<DiffProperty<DiffKind>> expected,
            DiffProperty<DiffKind> property, DiffKind along)
    {
        Projector<DiffProperty<DiffKind>, DiffKind> projector = new DiffPropertyProjector<>(new DiffKindProjector(),
                new DiffKindProjector());
        assertEquals(expected, projector.project(property, along));
    }

    /**
     * Returns test input, value to project along and expected output for
     * {@link #testDiffPropertyProjectorOnBothArguments}.
     *
     * @return Test input, value to project along and expected output.
     */
    private static Stream<Arguments> testDiffPropertyProjectorOnBothArgumentsData() {
        return Stream.of(
                // Projection should work correctly on the difference kind of a difference property.
                Arguments.of(Optional.of(new DiffProperty<>(DiffKind.ADDED, DiffKind.ADDED)),
                        new DiffProperty<>(DiffKind.ADDED, DiffKind.UNCHANGED), DiffKind.ADDED),

                // Projection should work correctly on the inner property of a difference property.
                Arguments.of(Optional.of(new DiffProperty<>(DiffKind.REMOVED, DiffKind.REMOVED)),
                        new DiffProperty<>(DiffKind.UNCHANGED, DiffKind.REMOVED), DiffKind.REMOVED),

                // Projection should leave nothing if projecting the inner property leaves nothing.
                Arguments.of(Optional.empty(), new DiffProperty<>(DiffKind.ADDED, DiffKind.UNCHANGED),
                        DiffKind.REMOVED),

                // Projection should leave nothing if projecting the difference kind leaves nothing.
                Arguments.of(Optional.empty(), new DiffProperty<>(DiffKind.UNCHANGED, DiffKind.REMOVED),
                        DiffKind.ADDED));
    }

    /**
     * Test {@link OptionalProjector}.
     *
     * @param expected Expected test output.
     * @param property Test property.
     * @param along The value to project along.
     */
    @ParameterizedTest
    @MethodSource("testOptionalProjectorData")
    public void testOptionalProjector(Optional<DiffKind> expected, Optional<DiffKind> property, DiffKind along) {
        Projector<Optional<DiffKind>, DiffKind> projector = new OptionalProjector<>(new DiffKindProjector());
        assertEquals(expected, projector.project(property, along));
    }

    /**
     * Returns test input, value to project along and expected output for {@link #testOptionalProjector}.
     *
     * @return Test input, value to project along and expected output.
     */
    private static Stream<Arguments> testOptionalProjectorData() {
        return Stream.of(
                // Projecting optional 'UNCHANGED' along 'ADDED' leaves an optional 'ADDED'.
                Arguments.of(Optional.of(Optional.of(DiffKind.ADDED)), Optional.of(DiffKind.UNCHANGED), DiffKind.ADDED),

                // Projecting optional 'REMOVED' along 'ADDED' leaves nothing.
                Arguments.of(Optional.empty(), Optional.of(DiffKind.REMOVED), DiffKind.ADDED),

                // Projecting an empty optional always leaves the empty optional.
                Arguments.of(Optional.of(Optional.empty()), Optional.empty(), DiffKind.ADDED));
    }

    /**
     * Test {@link DiffAutomatonStatePropertyProjector}.
     *
     * @param expected Expected test output.
     * @param property Test property.
     * @param along The value to project along.
     */
    @ParameterizedTest
    @MethodSource("testDiffAutomatonStatePropertyProjectorData")
    public void testDiffAutomatonStatePropertyProjector(Optional<DiffAutomatonStateProperty> expected,
            DiffAutomatonStateProperty property, DiffKind along)
    {
        Projector<DiffAutomatonStateProperty, DiffKind> projector = new DiffAutomatonStatePropertyProjector<>(
                new DiffKindProjector());
        assertEquals(expected, projector.project(property, along));
    }

    /**
     * Returns test input, value to project along and expected output for
     * {@link #testDiffAutomatonStatePropertyProjector}.
     *
     * @return Test input, value to project along and expected output.
     */
    private static Stream<Arguments> testDiffAutomatonStatePropertyProjectorData() {
        return Stream.of(
                // Projecting an 'UNCHANGED' non-initial state property along 'ADDED' leaves an 'ADDED' property.
                Arguments.of(Optional.of(new DiffAutomatonStateProperty(true, DiffKind.ADDED, Optional.empty())),
                        new DiffAutomatonStateProperty(true, DiffKind.UNCHANGED, Optional.empty()), DiffKind.ADDED),

                // Projecting an 'UNCHANGED' initial state property along 'ADDED' leaves an 'ADDED' property.
                Arguments.of(
                        Optional.of(new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.of(DiffKind.ADDED))),
                        new DiffAutomatonStateProperty(false, DiffKind.UNCHANGED, Optional.of(DiffKind.UNCHANGED)),
                        DiffKind.ADDED),

                // Projecting an 'ADDED' initial state arrow along 'REMOVED' removes the initial state arrow.
                Arguments.of(Optional.of(new DiffAutomatonStateProperty(true, DiffKind.REMOVED, Optional.empty())),
                        new DiffAutomatonStateProperty(true, DiffKind.UNCHANGED, Optional.of(DiffKind.ADDED)),
                        DiffKind.REMOVED),

                // Projecting an 'ADDED' state along 'REMOVED' leaves nothing.
                Arguments.of(Optional.empty(),
                        new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.of(DiffKind.ADDED)),
                        DiffKind.REMOVED));
    }

    /**
     * Test {@link SetProjector}.
     *
     * @param expected Expected test output.
     * @param properties Test properties.
     * @param along The value to project along.
     */
    @ParameterizedTest
    @MethodSource("testSetProjectorData")
    public void testSetProjector(Optional<Set<DiffKind>> expected, Set<DiffKind> properties, DiffKind along) {
        Projector<Set<DiffKind>, DiffKind> projector = new SetProjector<>(new DiffKindProjector());
        assertEquals(expected, projector.project(properties, along));
    }

    /**
     * Returns test inputs, value to project along and expected output for {@link #testSetProjector}.
     *
     * @return Test inputs, value to project along and expected output.
     */
    private static Stream<Arguments> testSetProjectorData() {
        return Stream.of(
                // Projecting an empty set over some difference kind leaves an empty set.
                Arguments.of(Optional.of(ImmutableSet.of()), ImmutableSet.of(), DiffKind.ADDED),

                // Projecting a set containing 'UNCHANGED' and 'ADDED' over 'ADDED' leaves a set with only 'ADDED'.
                Arguments.of(Optional.of(ImmutableSet.of(DiffKind.ADDED)),
                        ImmutableSet.of(DiffKind.UNCHANGED, DiffKind.ADDED), DiffKind.ADDED),

                // Projecting a set containing 'UNCHANGED' and 'ADDED' over 'REMOVED' leaves a set with only 'REMOVED'.
                Arguments.of(Optional.of(ImmutableSet.of(DiffKind.REMOVED)),
                        ImmutableSet.of(DiffKind.UNCHANGED, DiffKind.ADDED), DiffKind.REMOVED),

                // Projecting a set containing only 'ADDED' over 'REMOVED' leaves an empty set.
                Arguments.of(Optional.of(ImmutableSet.of()), ImmutableSet.of(DiffKind.ADDED), DiffKind.REMOVED));
    }
}
