//////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2021-2023 Contributors to the GitHub community
//
// This program and the accompanying materials are made available
// under the terms of the MIT License which is available at
// https://opensource.org/licenses/MIT
//
// SPDX-License-Identifier: MIT
//////////////////////////////////////////////////////////////////////////////

package com.github.tno.gltsdiff.operators.projectors;

import java.util.Optional;

import com.github.tno.gltsdiff.glts.DiffKind;
import com.google.common.base.Preconditions;

/** A projector for projecting {@link DiffKind difference kinds}. */
public class DiffKindProjector implements Projector<DiffKind, DiffKind> {
    @Override
    public Optional<DiffKind> project(DiffKind property, DiffKind along) {
        Preconditions.checkNotNull(property, "Expected a non-null property.");
        Preconditions.checkNotNull(along, "Expected a non-null element to project along.");

        if (property == along) {
            return Optional.of(property);
        } else if (property == DiffKind.UNCHANGED) {
            return Optional.of(along);
        } else {
            return Optional.empty();
        }
    }
}
