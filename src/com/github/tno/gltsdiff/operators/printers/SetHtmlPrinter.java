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

import java.util.Set;
import java.util.function.BinaryOperator;

import com.google.common.base.Preconditions;

/**
 * An HTML printer for sets of properties.
 *
 * @param <T> The type of properties.
 */
public class SetHtmlPrinter<T> implements HtmlPrinter<Set<T>> {
    /** The printer for properties. */
    private final HtmlPrinter<T> propertyPrinter;

    /** The accumulator for printed properties. */
    private final BinaryOperator<String> accumulator;

    /**
     * Instantiates a new property set printer, that separates all printed properties by spaces.
     * 
     * @param propertyPrinter The printer for properties.
     */
    public SetHtmlPrinter(HtmlPrinter<T> propertyPrinter) {
        this(propertyPrinter, (left, right) -> left + " " + right);
    }

    /**
     * Instantiates a new property set printer.
     * 
     * @param propertyPrinter The printer for properties.
     * @param accumulator The accumulator for printed properties.
     */
    public SetHtmlPrinter(HtmlPrinter<T> propertyPrinter, BinaryOperator<String> accumulator) {
        this.propertyPrinter = propertyPrinter;
        this.accumulator = accumulator;
    }

    @Override
    public String print(Set<T> set) {
        Preconditions.checkNotNull(set, "Expected a non-null set of properties.");
        return set.stream().map(propertyPrinter::print).reduce(accumulator).orElse("").trim();
    }
}
