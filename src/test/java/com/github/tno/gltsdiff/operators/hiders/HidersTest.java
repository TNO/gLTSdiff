//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.hiders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffKind;
import com.github.tno.gltsdiff.glts.lts.automaton.diff.DiffProperty;

/** {@link Hider} tests. */
public class HidersTest {
    /** Test {@link SubstitutionHider}. */
    @Test
    public void testSubstitutionHider() {
        Hider<String> hider = new SubstitutionHider<>("tau");
        assertEquals("tau", hider.hide("event"));
    }

    /**
     * Test {@link DiffPropertyHider}.
     *
     * @param input Test input.
     * @param expected Expected test output.
     */
    @ParameterizedTest
    @MethodSource("testDiffPropertyHiderData")
    public void testDiffPropertyHider(DiffProperty<String> input, DiffProperty<String> expected) {
        Hider<DiffProperty<String>> hider = new DiffPropertyHider<>(new SubstitutionHider<>("tau"));
        assertEquals(expected, hider.hide(input));
    }

    /**
     * Returns test input with expected output for {@link #testDiffPropertyHider}.
     *
     * @return Test input with expected output.
     */
    private static Stream<Arguments> testDiffPropertyHiderData() {
        return Stream.of(
                // Hiding should preserve the 'UNCHANGED' difference kind.
                Arguments.of(new DiffProperty<>("test", DiffKind.UNCHANGED),
                        new DiffProperty<>("tau", DiffKind.UNCHANGED)),

                // Hiding should preserve the 'ADDED' difference kind.
                Arguments.of(new DiffProperty<>("test", DiffKind.ADDED), new DiffProperty<>("tau", DiffKind.ADDED)),

                // Hiding should preserve the 'REMOVED' difference kind.
                Arguments.of(new DiffProperty<>("event", DiffKind.REMOVED),
                        new DiffProperty<>("tau", DiffKind.REMOVED)));
    }
}
