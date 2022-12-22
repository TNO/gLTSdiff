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

import com.github.tno.gltsdiff.glts.AnnotatedProperty;
import com.google.common.base.Preconditions;

/**
 * An HTML printer for {@link AnnotatedProperty annotated properties}.
 *
 * @param <T> The type of properties.
 * @param <U> The type of annotations.
 */
public class AnnotatedPropertyHtmlPrinter<T, U> implements HtmlPrinter<AnnotatedProperty<T, U>> {
    /** The printer for properties. */
    private final HtmlPrinter<T> propertyPrinter;

    /** The printer for sets of annotations. */
    private final HtmlPrinter<Set<U>> annotationPrinter;

    /**
     * Instantiates a new annotated property printer.
     * 
     * @param propertyPrinter The printer for properties.
     * @param annotationPrinter The printer for sets of annotations.
     */
    public AnnotatedPropertyHtmlPrinter(HtmlPrinter<T> propertyPrinter, HtmlPrinter<Set<U>> annotationPrinter) {
        this.propertyPrinter = propertyPrinter;
        this.annotationPrinter = annotationPrinter;
    }

    @Override
    public String print(AnnotatedProperty<T, U> property) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return (propertyPrinter.print(property.getProperty()) + " "
                + annotationPrinter.print(property.getAnnotations())).trim();
    }
}
