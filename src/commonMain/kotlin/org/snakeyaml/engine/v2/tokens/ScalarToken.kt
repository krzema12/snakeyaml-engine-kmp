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
package org.snakeyaml.engine.v2.tokens

import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.Mark
import kotlin.jvm.JvmOverloads

class ScalarToken @JvmOverloads constructor(
    val value: String,
    val plain: Boolean,
    startMark: Mark?,
    endMark: Mark?,
    val style: ScalarStyle = ScalarStyle.PLAIN,
) : Token(startMark, endMark) {

    override val tokenId: ID
        get() = ID.Scalar

    override fun toString(): String = "$tokenId plain=$plain style=$style value=$value"
}
