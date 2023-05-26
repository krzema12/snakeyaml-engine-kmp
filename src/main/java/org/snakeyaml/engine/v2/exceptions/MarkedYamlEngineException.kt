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
package org.snakeyaml.engine.v2.exceptions

/**
 * Parsing exception when the marks are available
 *
 * @param cause - exception which was thrown
 * @param context - the context of the problem
 * @param contextMark - position of the context
 * @param problem - the issue
 * @param problemMark - position of the issue
 */
open class MarkedYamlEngineException protected constructor(
    val context: String?,
    val contextMark: Mark?,
    val problem: String?,
    val problemMark: Mark?,
    cause: Throwable? = null,
) : YamlEngineException("$context; $problem; $problemMark", cause) {

    override val message: String
        get() = toString()

    /**
     * get readable error
     *
     * @return readable problem
     */
    override fun toString(): String = buildString {
        if (context != null) {
            appendLine(context)
        }
        if (contextMark != null) {
            val problemIsPresent = problem != null && problemMark != null

            if (
                !problemIsPresent
                || contextMark.name == problemMark?.name
                || contextMark.line != problemMark?.line
                || contextMark.column != problemMark.column
            ) {
                appendLine(contextMark)
            }
        }
        if (problem != null) {
            appendLine(problem)
        }
        if (problemMark != null) {
            appendLine(problemMark)
        }
    }
}
