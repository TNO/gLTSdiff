#!/usr/bin/bash

##############################################################################
# Copyright (c) 2020-2023 Contributors to the GitHub community
#
# This program and the accompanying materials are made available
# under the terms of the MIT License which is available at
# https://opensource.org/licenses/MIT
#
# SPDX-License-Identifier: MIT
##############################################################################

if [[ ! -f release.properties ]]; then
    echo "Need to prepare release first."
    exit 1
fi
mvn release:perform
echo
echo "Don't forgot push the changes and tags for this release."
echo "After a successful release, execute e.g. 'git push' and 'git push --tags'."
