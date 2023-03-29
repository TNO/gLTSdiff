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
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * An HTML printer for {@link Set sets} that joins together printed elements.
 *
 * @param <T> The type of elements of the sets.
 */
public class SetHtmlPrinter<T> implements HtmlPrinter<Set<T>> {
    /** The printer for set elements. */
    private final HtmlPrinter<T> elementPrinter;

    /** The prefix, as HTML. */
    private final String prefix;

    /** The delimiter, as HTML. */
    private final String delimiter;

    /** The suffix, as HTML. */
    private final String suffix;

    /**
     * Instantiates a new set printer, that uses "&#123;" and "&#125;" as prefix and suffix, respectively, delimiting
     * elements of the set by ", ".
     *
     * @param elementPrinter The printer for set elements.
     */
    public SetHtmlPrinter(HtmlPrinter<T> elementPrinter) {
        this(elementPrinter, "{", ", ", "}");
    }

    /**
     * Instantiates a new set printer.
     *
     * @param elementPrinter The printer for set elements.
     * @param prefix The prefix, as HTML.
     * @param delimiter The delimiter, as HTML.
     * @param suffix The suffix, as HTML.
     */
    public SetHtmlPrinter(HtmlPrinter<T> elementPrinter, String prefix, String delimiter, String suffix) {
        this.elementPrinter = elementPrinter;
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.suffix = suffix;
    }

    @Override
    public String print(Set<T> set) {
        Preconditions.checkNotNull(set, "Expected a non-null set.");
        return set.stream().map(elementPrinter::print).collect(Collectors.joining(delimiter, prefix, suffix)).trim();
    }
}
