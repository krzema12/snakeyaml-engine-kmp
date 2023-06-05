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
package org.snakeyaml.engine.v2.representer

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.RepresentToNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.Tag
import java.math.BigInteger
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Represent standard Java classes
 * @param settings - configuration options
 */
open class StandardRepresenter(
    private val settings: DumpSettings,
) : CommonRepresenter(settings) {

    /** Create [Node] for [Byte], [Short], [Int], [Long], [BigInteger], [Float], [Double] */
    private val representJvmNumber = RepresentToNode { data ->
        // TODO add test for very large numbers that can only be BigInteger
        if (data is BigInteger) {
            val value = data.toString()
            representScalar(
                getTag(data::class) { Tag.INT },
                value,
            )
        } else {
            representNumber.representData(data)
        }
    }

    /** Create Node for [UUID] */
    private val representUuid = RepresentToNode { data ->
        representScalar(
            getTag(data::class) { Tag.forType("java.util.UUID") },
            data.toString(),
        )
    }

    /** Create Node for [Optional] instance (the value of `null`) */
    private val representOptional = RepresentToNode { data ->
        val opt = (data as Optional<*>).getOrNull()
        if (opt != null) {
            represent(opt).apply {
                tag = Tag.forType("java.util.Optional")
            }
        } else {
            nullRepresenter()
        }
    }

    init {
        representers.putAll(
            mapOf(
                UUID::class to representUuid,
                Optional::class to representOptional,
            ),
        )

        parentClassRepresenters.putAll(
            mapOf(
                Number::class to representJvmNumber,
            ),
        )
    }
}
