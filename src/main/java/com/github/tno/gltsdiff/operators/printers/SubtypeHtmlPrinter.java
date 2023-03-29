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
 * An HTML printer for {@code U}-typed values based on a printer for {@code T}-typed values, with {@code U} a subtype of
 * {@code T}.
 *
 * @param <T> The type of values.
 * @param <U> The subtype of values.
 */
public class SubtypeHtmlPrinter<T, U extends T> implements HtmlPrinter<U> {
    /** The printer for values. */
    private final HtmlPrinter<T> printer;

    /**
     * Instantiates a new subtype printer.
     *
     * @param printer The printer for values.
     */
    public SubtypeHtmlPrinter(HtmlPrinter<T> printer) {
        this.printer = printer;
    }

    @Override
    public String print(U value) {
        return printer.print(value);
    }
}
