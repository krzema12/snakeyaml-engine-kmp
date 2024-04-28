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
package it.krzeminski.snakeyaml.engine.kmp.events

/**
 * The implicit flag of a scalar event is a pair of boolean values that indicate if the tag may be
 * omitted when the scalar is emitted in a plain and non-plain style correspondingly.
 */
class ImplicitTuple(
    private val plain: Boolean,
    private val nonPlain: Boolean,
) {
    /**
     * @return true when tag may be omitted when the scalar is emitted in a plain style.
     */
    fun canOmitTagInPlainScalar(): Boolean = plain

    /**
     * @return true when tag may be omitted when the scalar is emitted in a non-plain style.
     */
    fun canOmitTagInNonPlainScalar(): Boolean = nonPlain

    fun bothFalse(): Boolean = !plain && !nonPlain

    override fun toString(): String = "implicit=[$plain, $nonPlain]"
}
