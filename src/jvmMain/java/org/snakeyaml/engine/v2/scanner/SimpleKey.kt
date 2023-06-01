/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.v2.scanner

import org.snakeyaml.engine.v2.exceptions.Mark

/**
 * Simple keys treatment.
 *
 * Helper class for [ScannerImpl].
 */
internal class SimpleKey(
    val tokenNumber: Int,
    val isRequired: Boolean,
    val index: Int,
    val line: Int,
    val column: Int,
    val mark: Mark?,
) {
    override fun toString(): String =
        "SimpleKey - tokenNumber=$tokenNumber required=$isRequired index=$index line=$line column=$column"
}
