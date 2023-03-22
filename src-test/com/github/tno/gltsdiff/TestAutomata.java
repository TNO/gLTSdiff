//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff;

import org.apache.commons.math3.util.Pair;

import com.github.tno.gltsdiff.glts.AutomatonStateProperty;
import com.github.tno.gltsdiff.glts.SimpleAutomaton;
import com.github.tno.gltsdiff.glts.State;

/** Automata to use for testing. */
public class TestAutomata {
    /** Constructor for the {@link TestAutomata} class. */
    private TestAutomata() {
        // Static class.
    }

    /**
     * Returns a test automata pair. The LHS is an 'e1,e2' cycle of two states. The RHS is an 'e1,e2,e3' cycle of three
     * states.
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> smallThreeStateExample() {
        // Initialize and configure the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> a = lhs.addInitialState(true);
        State<AutomatonStateProperty> b = lhs.addState(true);

        lhs.addTransition(a, "e1", b);
        lhs.addTransition(b, "e2", a);

        // Initialize and configure RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> c = rhs.addInitialState(true);
        State<AutomatonStateProperty> d = rhs.addState(true);
        State<AutomatonStateProperty> e = rhs.addState(true);

        rhs.addTransition(c, "e1", d);
        rhs.addTransition(d, "e2", e);
        rhs.addTransition(e, "e3", c);

        return Pair.create(lhs, rhs);
    }

    /**
     * Returns a test automata pair matching the small example in Figure 3 of the article by Walkinshaw et al. (TOSEM
     * 2014).
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> smallExampleWalkinshaw() {
        // Initialize and configure the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> a = lhs.addInitialState(true);
        State<AutomatonStateProperty> b = lhs.addState(true);
        State<AutomatonStateProperty> c = lhs.addState(true);

        lhs.addTransition(a, "a", b);
        lhs.addTransition(b, "b", a);
        lhs.addTransition(b, "a", b);
        lhs.addTransition(b, "c", c);

        // Initialize and configure the RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> e = rhs.addInitialState(true);
        State<AutomatonStateProperty> f = rhs.addState(true);

        rhs.addTransition(e, "a", f);
        rhs.addTransition(e, "c", f);
        rhs.addTransition(f, "b", e);
        rhs.addTransition(f, "a", f);
        rhs.addTransition(f, "d", f);

        return Pair.create(lhs, rhs);
    }

    /**
     * Returns a test automata pair matching the running (text editor) example in the TOSEM 2014 article of Walkinshaw
     * et al. (Figure 1).
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> runningExampleWalkinshaw() {
        // Initialize and configure the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> sA = lhs.addInitialState(true);
        State<AutomatonStateProperty> sB = lhs.addState(true);
        State<AutomatonStateProperty> sC = lhs.addState(true);
        State<AutomatonStateProperty> sD = lhs.addState(true);

        lhs.addTransition(sA, "load", sB);
        lhs.addTransition(sA, "exit", sC);
        lhs.addTransition(sB, "close", sA);
        lhs.addTransition(sB, "edit", sB);
        lhs.addTransition(sB, "save as", sD);
        lhs.addTransition(sD, "ok", sB);

        // Initialize and configure the RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> sE = rhs.addInitialState(true);
        State<AutomatonStateProperty> sF = rhs.addState(true);
        State<AutomatonStateProperty> sG = rhs.addState(true);
        State<AutomatonStateProperty> sH = rhs.addState(true);
        State<AutomatonStateProperty> sI = rhs.addState(true);

        rhs.addTransition(sE, "load", sF);
        rhs.addTransition(sE, "exit", sH);
        rhs.addTransition(sF, "close", sE);
        rhs.addTransition(sF, "save as", sI);
        rhs.addTransition(sF, "edit", sG);
        rhs.addTransition(sG, "edit", sG);
        rhs.addTransition(sI, "ok", sF);

        return Pair.create(lhs, rhs);
    }

    /**
     * Returns a test automata pair matching an industrial example of two automata that are similar but not quite
     * identical.
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> industrialExample1() {
        // Initialize and configure the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> s1 = lhs.addInitialState(true);
        State<AutomatonStateProperty> s2 = lhs.addState(true);
        State<AutomatonStateProperty> s3 = lhs.addState(true);
        State<AutomatonStateProperty> s4 = lhs.addState(true);

        lhs.addTransition(s1, "e1", s2);
        lhs.addTransition(s2, "e2", s3);
        lhs.addTransition(s3, "e3", s4);
        lhs.addTransition(s4, "e4", s1);

        // Initialize and configure the RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> t1 = rhs.addInitialState(true);
        State<AutomatonStateProperty> t2 = rhs.addState(true);

        rhs.addTransition(t1, "e1", t2);
        rhs.addTransition(t2, "e4", t1);

        return Pair.create(lhs, rhs);
    }

    /**
     * Returns a test automata pair for a slightly bigger industrial example (with respect to
     * {@link #industrialExample1}) of two automata that are similar but not quite identical.
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> industrialExample2() {
        // Initialize and configure the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> s4 = lhs.addInitialState(true);
        State<AutomatonStateProperty> s5 = lhs.addState(true);
        State<AutomatonStateProperty> s6 = lhs.addState(true);
        State<AutomatonStateProperty> s7 = lhs.addState(true);
        State<AutomatonStateProperty> s8 = lhs.addState(true);
        State<AutomatonStateProperty> s9 = lhs.addState(true);
        State<AutomatonStateProperty> s10 = lhs.addState(true);
        State<AutomatonStateProperty> s11 = lhs.addState(true);
        State<AutomatonStateProperty> s12 = lhs.addState(true);
        State<AutomatonStateProperty> s13 = lhs.addState(true);
        State<AutomatonStateProperty> s14 = lhs.addState(true);
        State<AutomatonStateProperty> s15 = lhs.addState(true);
        State<AutomatonStateProperty> s16 = lhs.addState(true);

        lhs.addTransition(s4, "e19", s5);
        lhs.addTransition(s5, "e8", s6);
        lhs.addTransition(s5, "e1", s7);
        lhs.addTransition(s6, "e9", s8);
        lhs.addTransition(s7, "e2", s9);
        lhs.addTransition(s8, "e3", s4);
        lhs.addTransition(s9, "e6", s10);
        lhs.addTransition(s10, "e7", s11);
        lhs.addTransition(s11, "e8", s12);
        lhs.addTransition(s12, "e9", s13);
        lhs.addTransition(s13, "e4", s14);
        lhs.addTransition(s14, "e5", s15);
        lhs.addTransition(s15, "e10", s16);
        lhs.addTransition(s16, "e11", s8);

        // Initialize and configure the RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> t1 = rhs.addState(true);
        State<AutomatonStateProperty> t2 = rhs.addState(true);
        State<AutomatonStateProperty> t3 = rhs.addState(true);
        State<AutomatonStateProperty> t4 = rhs.addInitialState(true);
        State<AutomatonStateProperty> t5 = rhs.addState(true);
        State<AutomatonStateProperty> t6 = rhs.addState(true);
        State<AutomatonStateProperty> t8 = rhs.addState(true);
        State<AutomatonStateProperty> t9 = rhs.addState(true);
        State<AutomatonStateProperty> t10 = rhs.addState(true);
        State<AutomatonStateProperty> t11 = rhs.addState(true);
        State<AutomatonStateProperty> t12 = rhs.addState(true);
        State<AutomatonStateProperty> t13 = rhs.addState(true);
        State<AutomatonStateProperty> t14 = rhs.addState(true);
        State<AutomatonStateProperty> t15 = rhs.addState(true);

        rhs.addTransition(t1, "e13", t9);
        rhs.addTransition(t2, "e15", t3);
        rhs.addTransition(t3, "e16", t13);
        rhs.addTransition(t4, "e19", t5);
        rhs.addTransition(t5, "e8", t6);
        rhs.addTransition(t6, "e9", t8);
        rhs.addTransition(t8, "e3", t4);
        rhs.addTransition(t8, "e14", t1);
        rhs.addTransition(t9, "e12", t10);
        rhs.addTransition(t10, "e20", t11);
        rhs.addTransition(t11, "e17", t12);
        rhs.addTransition(t12, "e18", t2);
        rhs.addTransition(t13, "e4", t14);
        rhs.addTransition(t14, "e5", t15);
        rhs.addTransition(t15, "e3", t4);

        return Pair.create(lhs, rhs);
    }

    /**
     * Returns a test automata pair of two example automata that are both 3-state cycles and share the same alphabet,
     * but with a slightly different order of events: one is a 'b,d,c' cycle while the other is a 'b,c,d' cycle.
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> smallThreeStateLoopWithSwappedEvents() {
        // Initialize and configure the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> s1 = lhs.addInitialState(true);
        State<AutomatonStateProperty> s2 = lhs.addState(true);
        State<AutomatonStateProperty> s3 = lhs.addState(true);

        lhs.addTransition(s1, "b", s2);
        lhs.addTransition(s2, "d", s3);
        lhs.addTransition(s3, "c", s1);

        // Initialize and configure the RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> t1 = rhs.addInitialState(true);
        State<AutomatonStateProperty> t2 = rhs.addState(true);
        State<AutomatonStateProperty> t3 = rhs.addState(true);

        rhs.addTransition(t1, "b", t2);
        rhs.addTransition(t2, "c", t3);
        rhs.addTransition(t3, "d", t1);

        return Pair.create(lhs, rhs);
    }

    /**
     * Returns a test automata pair to be used for testing the brute force scorer and matcher.
     *
     * @return The test automata pair.
     */
    public static Pair<SimpleAutomaton<String>, SimpleAutomaton<String>> smallAutomataForBruteForceTesting() {
        // Define the LHS automaton.
        SimpleAutomaton<String> lhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> s1 = lhs.addInitialState(true);
        State<AutomatonStateProperty> s2 = lhs.addState(true);
        State<AutomatonStateProperty> s3 = lhs.addState(true);

        lhs.addTransition(s1, "b", s2);
        lhs.addTransition(s2, "c", s3);
        lhs.addTransition(s3, "d", s1);

        // Define the RHS automaton.
        SimpleAutomaton<String> rhs = new SimpleAutomaton<>();

        State<AutomatonStateProperty> t1 = rhs.addInitialState(true);
        State<AutomatonStateProperty> t2 = rhs.addState(true);
        State<AutomatonStateProperty> t3 = rhs.addState(true);
        State<AutomatonStateProperty> t4 = rhs.addState(true);

        rhs.addTransition(t1, "b", t2);
        rhs.addTransition(t2, "c", t3);
        rhs.addTransition(t3, "d", t1);
        rhs.addTransition(t2, "b", t4);
        rhs.addTransition(t4, "c", t3);

        return Pair.create(lhs, rhs);
    }
}
