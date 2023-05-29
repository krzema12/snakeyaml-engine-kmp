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
package org.snakeyaml.engine.v2.resolver

import org.snakeyaml.engine.v2.nodes.Tag
import java.util.regex.Pattern

/**
 * Base resolver
 */
abstract class BaseScalarResolver @JvmOverloads constructor(
    /**
     * Register all the resolvers to be applied
     */
    buildImplicitResolvers: ImplicitResolversBuilder.() -> Unit = {},
) : ScalarResolver {

    /** Map from the char to the resolver which may begin with this char */
    private val yamlImplicitResolvers: Map<Char?, List<ResolverTuple>>

    init {
        yamlImplicitResolvers = ImplicitResolversBuilder().apply(buildImplicitResolvers).resolvers()
    }

    override fun resolve(value: String, implicit: Boolean): Tag {
        if (!implicit) return Tag.STR

        val resolverKey = value.getOrNull(0) ?: '\u0000'

        val resolvers = yamlImplicitResolvers[resolverKey]
            ?: yamlImplicitResolvers[null]
            ?: emptyList()

        return resolvers
            .firstOrNull { v -> v.regexp.matcher(value).matches() }
            ?.tag
            ?: Tag.STR
    }

    class ImplicitResolversBuilder {
        private val resolvers: MutableMap<Char?, MutableList<ResolverTuple>> = mutableMapOf()

        internal fun resolvers(): Map<Char?, MutableList<ResolverTuple>> = resolvers.toMap()

        /**
         * Add a resolver to resolve a value that matches the provided regular expression to the provided
         * tag
         *
         * @param tag    the Tag to assign when the value matches
         * @param regexp the RE which is applied for every value
         * @param first  the possible first characters (this is merely for performance improvement) to
         * skip RE evaluation to gain time
         */
        fun addImplicitResolver(tag: Tag, regexp: Pattern, first: String?) {
            val keys = first?.toCharArray()
                ?.map { chr ->
                    // special case: for null
                    if (chr.code == 0) null else chr
                }
                ?: listOf(null)

            for (key in keys.distinct()) {
                val curr = resolvers.getOrPut(key) { mutableListOf() }
                curr.add(ResolverTuple(tag, regexp))
            }
        }
    }

    companion object {
        /**
         * No value indication
         */
        val EMPTY: Pattern = Pattern.compile("^$")

        /**
         * group 1: name, group 2: separator, group 3: value
         */
        @JvmField
        val ENV_FORMAT = Regex("^\\$\\{\\s*(\\w+)(?:(:?[-?])(\\w+)?)?\\s*}$")
    }
}
