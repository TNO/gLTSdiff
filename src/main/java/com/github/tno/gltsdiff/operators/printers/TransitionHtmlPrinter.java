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

import com.github.tno.gltsdiff.glts.Transition;
import com.google.common.base.Preconditions;

/**
 * An HTML printer that prints transitions by printing their properties.
 *
 * @param <S> The type of state properties.
 * @param <T> The type of transition properties.
 */
public class TransitionHtmlPrinter<S, T> implements HtmlPrinter<Transition<S, T>> {
    /** The printer for transition properties. */
    private final HtmlPrinter<T> propertyPrinter;

    /**
     * Instantiates a new transition printer.
     *
     * @param propertyPrinter The printer for transition properties.
     */
    public TransitionHtmlPrinter(HtmlPrinter<T> propertyPrinter) {
        this.propertyPrinter = propertyPrinter;
    }

    @Override
    public String print(Transition<S, T> transition) {
        Preconditions.checkNotNull(transition, "Expected a non-null transition.");
        Preconditions.checkNotNull(transition.getProperty(), "Expected a non-null transition property.");
        return propertyPrinter.print(transition.getProperty());
    }
}
