//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.writers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.github.tno.gltsdiff.glts.State;
import com.github.tno.gltsdiff.glts.lts.automaton.Automaton;
import com.github.tno.gltsdiff.glts.lts.automaton.AutomatonStateProperty;
import com.github.tno.gltsdiff.writers.lts.automaton.AutomatonDotWriter;

/** {@link DotWriter} tests. */
public class DotWriterTest {
    /**
     * Test writing non-ASCII output.
     *
     * @throws IOException In case of an I/O error.
     */
    @Test
    public void testNonAscii() throws IOException {
        Automaton<String> aut = new Automaton<>();
        State<AutomatonStateProperty> a = aut.addState(new AutomatonStateProperty(true, true));
        State<AutomatonStateProperty> b = aut.addState(new AutomatonStateProperty(false, false));
        aut.addTransition(a, "true → x := x + 1", b);
        AutomatonDotWriter<AutomatonStateProperty, String, Automaton<String>> writer = new AutomatonDotWriter<>(
                t -> t.getProperty());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.write(aut, stream);
        String actual = stream.toString(StandardCharsets.UTF_8);
        String expected = "digraph automaton {\r\n"
                + "\t1 [label=<s1> shape=\"doublecircle\"];\r\n"
                + "\t2 [label=<s2> shape=\"circle\"];\r\n"
                + "\t__init1 [label=<> shape=\"none\"];\r\n"
                + "\t__init1 -> 1;\r\n"
                + "\t1 -> 2 [label=<true → x := x + 1> id=\"1-0-2\"];\r\n"
                + "}\r\n"
                + "";
        assertEquals(expected, actual);
    }
}
