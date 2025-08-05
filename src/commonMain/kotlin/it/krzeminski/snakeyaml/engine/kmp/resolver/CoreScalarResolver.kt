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
package it.krzeminski.snakeyaml.engine.kmp.resolver

import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import kotlin.jvm.JvmField


/**
 * ScalarResolver for Core Schema with the merge implemented.
 * See [Support of Merge Keys](https://ktomk.github.io/writing/yaml-anchor-alias-and-merge-key.html).
 */
class CoreScalarResolver(supportMerge: Boolean) : BaseScalarResolver(
    {
        addImplicitResolver(Tag.NULL, EMPTY, null)
        addImplicitResolver(Tag.BOOL, BOOL, "tfTF")
        // INT must be before FLOAT because the regular expression for FLOAT matches INT
        addImplicitResolver(Tag.INT, INT, "-+0123456789")
        addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.")
        addImplicitResolver(Tag.NULL, NULL, "n\u0000")
        addImplicitResolver(Tag.ENV_TAG, ENV_FORMAT, "$")
        if (supportMerge) {
            addImplicitResolver(Tag.MERGE, MERGE, "<")
        }
    },
) {

    companion object {

        /** Boolean as defined in Core */
        val BOOL = Regex("^(?:true|True|TRUE|false|False|FALSE)$")

        /**
         * Float as defined in JSON (Number which is Float).
         *
         * Be aware that this regex will also match integers.
         */
        val FLOAT = Regex(
            """^(?:([-+]?(\.[0-9]+|[0-9]+(\.[0-9]*)?)([eE][-+]?[0-9]+)?)""" + // float
                    """|([-+]?\.(?:inf|Inf|INF))""" + // infinity
                    """|(\.(?:nan|NaN|NAN)))$""", // not a number
        )

        /**
         * Merge is optional, but not defined in YAML 1.2
         */
        @JvmField
        val MERGE = Regex("^(?:<<)$")

        /** Integer as defined in Core */
        @JvmField
        val INT = Regex(
            "^(?:([-+]?[0-9]+)" + // (base 10)
                    "|(0o[0-7]+)" + // (base 8)
                    "|(0x[0-9a-fA-F]+))$", // (base 16)
        )

        /** Null as defined in Core */
        val NULL = Regex("^(?:~|null|Null|NULL| )$")

    }
}
