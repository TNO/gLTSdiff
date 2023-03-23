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
     * @param property Test property.
     * @param along The value to project along.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testDiffKindProjectorData")
    public void testDiffKindProjector(DiffKind property, DiffKind along, Optional<DiffKind> expected) {
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
                Arguments.of(DiffKind.ADDED, DiffKind.ADDED, Optional.of(DiffKind.ADDED)),

                // Projecting 'UNCHANGED' along 'REMOVED' should leave 'REMOVED'.
                Arguments.of(DiffKind.UNCHANGED, DiffKind.REMOVED, Optional.of(DiffKind.REMOVED)),

                // Projecting 'ADDED' along 'REMOVED' should leave nothing.
                Arguments.of(DiffKind.ADDED, DiffKind.REMOVED, Optional.empty()),

                // Projecting 'REMOVED' along 'UNCHANGED' should leave nothing.
                Arguments.of(DiffKind.REMOVED, DiffKind.UNCHANGED, Optional.empty()));
    }

    /**
     * Test {@link DiffPropertyProjector} on right argument.
     *
     * @param <T> The type of properties.
     * @param property Test property.
     * @param along The value to project along.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testDiffPropertyProjectorOnRightArgumentData")
    public <T> void testDiffPropertyProjectorOnRightArgument(DiffProperty<T> property, DiffKind along,
            Optional<DiffProperty<T>> expected)
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
                Arguments.of(new DiffProperty<>("e", DiffKind.UNCHANGED), DiffKind.REMOVED,
                        Optional.of(new DiffProperty<>("e", DiffKind.REMOVED))),

                // Projecting an 'ADDED' difference property along 'REMOVED' should leave nothing.
                Arguments.of(new DiffProperty<>("e", DiffKind.ADDED), DiffKind.REMOVED, Optional.empty()));
    }

    /**
     * Test {@link DiffPropertyProjector} on both arguments.
     *
     * @param property Test property.
     * @param along The value to project along.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testDiffPropertyProjectorOnBothArgumentsData")
    public void testDiffPropertyProjectorOnBothArguments(DiffProperty<DiffKind> property, DiffKind along,
            Optional<DiffProperty<DiffKind>> expected)
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
                Arguments.of(new DiffProperty<>(DiffKind.ADDED, DiffKind.UNCHANGED), DiffKind.ADDED,
                        Optional.of(new DiffProperty<>(DiffKind.ADDED, DiffKind.ADDED))),

                // Projection should work correctly on the inner property of a difference property.
                Arguments.of(new DiffProperty<>(DiffKind.UNCHANGED, DiffKind.REMOVED), DiffKind.REMOVED,
                        Optional.of(new DiffProperty<>(DiffKind.REMOVED, DiffKind.REMOVED))),

                // Projection should leave nothing if projecting the inner property leaves nothing.
                Arguments.of(new DiffProperty<>(DiffKind.ADDED, DiffKind.UNCHANGED), DiffKind.REMOVED,
                        Optional.empty()),

                // Projection should leave nothing if projecting the difference kind leaves nothing.
                Arguments.of(new DiffProperty<>(DiffKind.UNCHANGED, DiffKind.REMOVED), DiffKind.ADDED,
                        Optional.empty()));
    }

    /**
     * Test {@link OptionalProjector}.
     *
     * @param property Test property.
     * @param along The value to project along.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testOptionalProjectorData")
    public void testOptionalProjector(Optional<DiffKind> property, DiffKind along, Optional<DiffKind> expected) {
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
                Arguments.of(Optional.of(DiffKind.UNCHANGED), DiffKind.ADDED, Optional.of(Optional.of(DiffKind.ADDED))),

                // Projecting optional 'REMOVED' along 'ADDED' leaves nothing.
                Arguments.of(Optional.of(DiffKind.REMOVED), DiffKind.ADDED, Optional.empty()),

                // Projecting an empty optional always leaves the empty optional.
                Arguments.of(Optional.empty(), DiffKind.ADDED, Optional.of(Optional.empty())));
    }

    /**
     * Test {@link DiffAutomatonStatePropertyProjector}.
     *
     * @param property Test property.
     * @param along The value to project along.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testDiffAutomatonStatePropertyProjectorData")
    public void testDiffAutomatonStatePropertyProjector(DiffAutomatonStateProperty property, DiffKind along,
            Optional<DiffAutomatonStateProperty> expected)
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
                Arguments.of(new DiffAutomatonStateProperty(true, DiffKind.UNCHANGED, Optional.empty()), DiffKind.ADDED,
                        Optional.of(new DiffAutomatonStateProperty(true, DiffKind.ADDED, Optional.empty()))),

                // Projecting an 'UNCHANGED' initial state property along 'ADDED' leaves an 'ADDED' property.
                Arguments.of(new DiffAutomatonStateProperty(false, DiffKind.UNCHANGED, Optional.of(DiffKind.UNCHANGED)),
                        DiffKind.ADDED,
                        Optional.of(
                                new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.of(DiffKind.ADDED)))),

                // Projecting an 'ADDED' initial state arrow along 'REMOVED' removes the initial state arrow.
                Arguments.of(new DiffAutomatonStateProperty(true, DiffKind.UNCHANGED, Optional.of(DiffKind.ADDED)),
                        DiffKind.REMOVED,
                        Optional.of(new DiffAutomatonStateProperty(true, DiffKind.REMOVED, Optional.empty()))),

                // Projecting an 'ADDED' state along 'REMOVED' leaves nothing.
                Arguments.of(new DiffAutomatonStateProperty(false, DiffKind.ADDED, Optional.of(DiffKind.ADDED)),
                        DiffKind.REMOVED, Optional.empty()));
    }

    /**
     * Test {@link SetProjector}.
     *
     * @param properties Test properties.
     * @param along The value to project along.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testSetProjectorData")
    public void testSetProjector(Set<DiffKind> properties, DiffKind along, Optional<Set<DiffKind>> expected) {
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
                Arguments.of(ImmutableSet.of(), DiffKind.ADDED, Optional.of(ImmutableSet.of())),

                // Projecting a set containing 'UNCHANGED' and 'ADDED' over 'ADDED' leaves a set with only 'ADDED'.
                Arguments.of(ImmutableSet.of(DiffKind.UNCHANGED, DiffKind.ADDED), DiffKind.ADDED,
                        Optional.of(ImmutableSet.of(DiffKind.ADDED))),

                // Projecting a set containing 'UNCHANGED' and 'ADDED' over 'REMOVED' leaves a set with only 'REMOVED'.
                Arguments.of(ImmutableSet.of(DiffKind.UNCHANGED, DiffKind.ADDED), DiffKind.REMOVED,
                        Optional.of(ImmutableSet.of(DiffKind.REMOVED))),

                // Projecting a set containing only 'ADDED' over 'REMOVED' leaves an empty set.
                Arguments.of(ImmutableSet.of(DiffKind.ADDED), DiffKind.REMOVED, Optional.of(ImmutableSet.of())));
    }
}
