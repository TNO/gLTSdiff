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

import com.github.tno.gltsdiff.glts.DiffKind;
import com.github.tno.gltsdiff.glts.DiffProperty;
import com.google.common.base.Preconditions;

/**
 * An HTML printer for printing {@link DiffProperty difference properties}.
 *
 * @param <T> The type of inner properties.
 */
public class DiffPropertyHtmlPrinter<T> implements HtmlPrinter<DiffProperty<T>> {
    /** The printer for the inner properties. */
    private final HtmlPrinter<T> propertyPrinter;

    /** The printer for colors for difference kinds. */
    private final HtmlPrinter<DiffKind> diffKindColorPrinter = new DiffKindHtmlPrinter();

    /**
     * Instantiates a new difference property printer.
     * 
     * @param propertyPrinter The printer for the inner properties.
     */
    public DiffPropertyHtmlPrinter(HtmlPrinter<T> propertyPrinter) {
        this.propertyPrinter = propertyPrinter;
    }

    @Override
    public String print(DiffProperty<T> property) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return String.format("<font color=\"%s\">%s</font>", diffKindColorPrinter.print(property.getDiffKind()),
                propertyPrinter.print(property.getProperty()));
    }
}
