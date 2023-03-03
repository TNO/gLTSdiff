//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2022-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.printers;

import org.apache.commons.math3.util.Pair;

/**
 * An HTML printer for {@link Pair} properties.
 *
 * @param <T> The type of the first elements of the pairs.
 * @param <U> The type of the second elements of the pairs.
 */
public class PairHtmlPrinter<T, U> implements HtmlPrinter<Pair<T, U>> {
    /** The prefix HTML text. */
    private final String prefix;

    /** The printer for first elements of the pairs. */
    private final HtmlPrinter<T> firstPrinter;

    /** The separator HTML text. */
    private final String separator;

    /** The printer for second elements of the pairs. */
    private final HtmlPrinter<U> secondPrinter;

    /** The suffix HTML text. */
    private final String suffix;

    /**
     * Instantiates a new pair property printer, that uses "(" as prefix HTML text, "," as separator HTML text, and ")"
     * as suffix HTML text.
     *
     * @param firstPrinter The printer for first elements of the pairs.
     * @param secondPrinter The printer for second elements of the pairs.
     */
    public PairHtmlPrinter(HtmlPrinter<T> firstPrinter, HtmlPrinter<U> secondPrinter) {
        this("(", firstPrinter, ", ", secondPrinter, ")");
    }

    /**
     * Instantiates a new pair property printer.
     *
     * @param prefix The prefix HTML text.
     * @param firstPrinter The printer for first elements of the pairs.
     * @param separator The separator HTML text.
     * @param secondPrinter The printer for second elements of the pairs.
     * @param suffix The suffix HTML text.
     */
    public PairHtmlPrinter(String prefix, HtmlPrinter<T> firstPrinter, String separator, HtmlPrinter<U> secondPrinter,
            String suffix)
    {
        this.prefix = prefix;
        this.firstPrinter = firstPrinter;
        this.separator = separator;
        this.secondPrinter = secondPrinter;
        this.suffix = suffix;
    }

    @Override
    public String print(Pair<T, U> pair) {
        return prefix + firstPrinter.print(pair.getFirst()).trim() + separator
                + secondPrinter.print(pair.getSecond()).trim() + suffix;
    }
}
