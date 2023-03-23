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
import com.google.common.base.Preconditions;

/** An HTML printer that prints the hex color associated to a specified {@link DiffKind difference kind}. */
public class DiffKindHtmlPrinter implements HtmlPrinter<DiffKind> {
    /** The hex representation of green. */
    private static final String COLOR_GREEN = "#00cc00";

    /** The hex representation of red. */
    private static final String COLOR_RED = "#ff4040";

    /** The hex representation of black. */
    private static final String COLOR_BLACK = "#000000";

    @Override
    public String print(DiffKind diffKind) {
        Preconditions.checkNotNull(diffKind, "Expected a non-null difference kind.");

        switch (diffKind) {
            case ADDED:
                return COLOR_GREEN;
            case REMOVED:
                return COLOR_RED;
            case UNCHANGED:
                return COLOR_BLACK;
            default:
                throw new RuntimeException("Unknown difference kind.");
        }
    }
}
