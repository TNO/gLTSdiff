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
 * A printer, an operator for printing values, such as state and transition labels, in HTML format.
 *
 * @param <T> The type of properties.
 */
@FunctionalInterface
public interface HtmlPrinter<T> {
    /**
     * Prints the specified value as HTML.
     *
     * @param value The non-{@code null} value to print.
     * @return The value printed as HTML.
     */
    public String print(T value);
}
