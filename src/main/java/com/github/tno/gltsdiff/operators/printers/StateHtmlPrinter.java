//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.printers;

import com.github.tno.gltsdiff.glts.State;
import com.google.common.base.Preconditions;

/**
 * An HTML printer that prints states by prefixing their state IDs.
 *
 * <p>
 * While {@link State} {@link State#getId IDs} are 0-based, this printer uses 1-based IDs. This leads to nicer output,
 * as the first state then has ID 1, while the last state has the number of states as its ID.
 * </p>
 *
 * @param <S> The type of state properties.
 */
public class StateHtmlPrinter<S> implements HtmlPrinter<State<S>> {
    /** The prefix, as HTML. */
    private final String prefix;

    /** Instantiates a new state printer. Uses "s" as prefix. */
    public StateHtmlPrinter() {
        this("s");
    }

    /**
     * Instantiates a new state printer.
     *
     * @param prefix The prefix, as HTML.
     */
    public StateHtmlPrinter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String print(State<S> state) {
        Preconditions.checkNotNull(state, "Expected a non-null state.");
        int id = state.getId() + 1;
        return prefix + Integer.toString(id);
    }
}
