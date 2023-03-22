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

import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;

public class HidersTest {
    @Test
    public void testSubstitutionHider() {
        Hider<String> hider = new SubstitutionHider<>("tau");
        assertEquals("tau", hider.hide("event"));
    }

    @ParameterizedTest()
    @MethodSource("testDiffKindProjectorProvider")
    public void testDiffPropertyHider(DiffProperty<String> expected, DiffProperty<String> input) {
        Hider<DiffProperty<String>> hider = new DiffPropertyHider<>(new SubstitutionHider<>("tau"));
        assertEquals(expected, hider.hide(input));
    }

    private static Stream<Arguments> testDiffKindProjectorProvider() {
        return Stream.of(
                // Hiding should preserve the 'UNCHANGED' difference kind.
                Arguments.of(new DiffProperty<>("tau", DiffKind.UNCHANGED),
                        new DiffProperty<>("test", DiffKind.UNCHANGED)),
                // Hiding should preserve the 'ADDED' difference kind.
                Arguments.of(new DiffProperty<>("tau", DiffKind.ADDED), new DiffProperty<>("test", DiffKind.ADDED)),
                // Hiding should preserve the 'REMOVED' difference kind.
                Arguments.of(new DiffProperty<>("tau", DiffKind.REMOVED),
                        new DiffProperty<>("event", DiffKind.REMOVED)));
    }
}
