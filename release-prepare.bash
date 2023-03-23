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

mvn clean
rm -f pom.xml.releaseBackup
rm -f release.properties
mvn release:prepare
