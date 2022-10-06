//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2022 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.printers;

import java.util.Set;

import com.google.common.base.Preconditions;

/**
 * An HTML printer for sets of properties.
 *
 * @param <T> The type of properties.
 */
public class SetHtmlPrinter<T> implements HtmlPrinter<Set<T>> {
    /** The printer for properties. */
    private final HtmlPrinter<T> propertyPrinter;

    /**
     * Instantiates a new property set printer.
     * 
     * @param propertyPrinter The printer for properties.
     */
    public SetHtmlPrinter(HtmlPrinter<T> propertyPrinter) {
        this.propertyPrinter = propertyPrinter;
    }

    @Override
    public String print(Set<T> set) {
        Preconditions.checkNotNull(set, "Expected a non-null set of properties.");
        return set.stream().map(propertyPrinter::print).reduce("", (left, right) -> left + " " + right).trim();
    }
}
