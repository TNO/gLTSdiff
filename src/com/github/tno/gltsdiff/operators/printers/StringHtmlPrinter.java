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

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.base.Preconditions;

/**
 * An HTML printer that prints properties as strings, thereby applying HTML escaping.
 *
 * @param <T> The type of properties.
 */
public class StringHtmlPrinter<T> implements HtmlPrinter<T> {
    @Override
    public String print(T property) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        return StringEscapeUtils.escapeHtml(property.toString().trim());
    }
}
