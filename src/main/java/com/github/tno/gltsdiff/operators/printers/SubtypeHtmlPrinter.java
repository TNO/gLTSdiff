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

/**
 * An HTML printer for {@code U}-typed properties based on a printer for {@code T}-typed properties, with {@code U} a
 * subtype of {@code T}.
 *
 * @param <T> The type of properties.
 * @param <U> The subtype of properties.
 */
public class SubtypeHtmlPrinter<T, U extends T> implements HtmlPrinter<U> {
    /** The printer for properties. */
    private final HtmlPrinter<T> printer;

    /**
     * Instantiates a new subtype property printer.
     *
     * @param printer The printer for properties.
     */
    public SubtypeHtmlPrinter(HtmlPrinter<T> printer) {
        this.printer = printer;
    }

    @Override
    public String print(U property) {
        return printer.print(property);
    }
}
