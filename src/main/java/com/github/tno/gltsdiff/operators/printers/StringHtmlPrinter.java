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

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.base.Preconditions;

/**
 * An HTML printer that prints values by converting them {@link Object#toString to strings} and applying HTML escaping.
 *
 * @param <T> The type of values.
 */
public class StringHtmlPrinter<T> implements HtmlPrinter<T> {
    @Override
    public String print(T value) {
        Preconditions.checkNotNull(value, "Expected a non-null value.");
        return StringEscapeUtils.escapeHtml4(value.toString().trim());
    }
}
