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
package it.krzeminski.snakeyaml.engine.kmp.common

/**
 * YAML Version indicator
 * @param major - major part of version, must be 1
 * @param minor - minor part of version, may be 0 or 1
 */
class SpecVersion(
    val major: Int,
    val minor: Int,
) {
    /** create readable text */
    val representation: String
        get() = "$major.$minor"

    override fun toString(): String = "Version{major=$major, minor=$minor}"
}
