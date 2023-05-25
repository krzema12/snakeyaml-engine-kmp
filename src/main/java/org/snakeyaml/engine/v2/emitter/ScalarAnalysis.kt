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
package org.snakeyaml.engine.v2.emitter

/**
 * Accumulate information to choose the scalar style
 *
 * @param scalar - the data to analyse
 * @param empty - true for empty scalar
 * @param multiline - true if it may take many lines
 * @param allowFlowPlain - `true` if can be plain in flow context
 * @param allowBlockPlain - `true` if can be plain in block context
 * @param allowSingleQuoted - true if single quotes are allowed
 * @param allowBlock - true if block style is allowed
 */
class ScalarAnalysis(
    /** the scalar to be analysed */
    private val scalar: String,
    /** `true` when empty */
    private val empty: Boolean,
    /**
     * getter
     *
     * @return true if it may take many lines
     */
    private val multiline: Boolean,
    /**
     * getter
     *
     * @return `true` if can be plain in flow context
     */
    private val allowFlowPlain: Boolean,
    /**
     * getter
     *
     * @return true if can be plain in block context
     */
    private val allowBlockPlain: Boolean,
    /**
     * getter
     *
     * @return true if single quotes are allowed
     */
    private val allowSingleQuoted: Boolean,
    /**
     * getter
     *
     * @return true when block style is allowed for this scalar
     */
    private val allowBlock: Boolean,
) {

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("scalar"))
    fun getScalar(): String = scalar

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("empty"))
    fun isEmpty(): Boolean = empty

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("multiline"))
    fun isMultiline(): Boolean = multiline

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("allowFlowPlain"))
    fun isAllowFlowPlain(): Boolean = allowFlowPlain

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("allowBlockPlain"))
    fun isAllowBlockPlain(): Boolean = allowBlockPlain

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("allowSingleQuoted"))
    fun isAllowSingleQuoted(): Boolean = allowSingleQuoted

    //@Deprecated("temp helper for java->jt migration", ReplaceWith("allowBlock"))
    fun isAllowBlock(): Boolean = allowBlock
}
