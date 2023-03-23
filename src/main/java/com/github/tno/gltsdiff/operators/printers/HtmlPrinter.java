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
 * A printer, an operator for printing state and/or transition properties in HTML format.
 *
 * @param <T> The type of properties.
 */
@FunctionalInterface
public interface HtmlPrinter<T> {
    /**
     * Prints the specified property as HTML.
     *
     * @param property The non-{@code null} property to print.
     * @return The printed property, as HTML.
     */
    public String print(T property);
}
