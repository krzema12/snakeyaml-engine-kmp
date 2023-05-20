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

import org.snakeyaml.engine.v2.parser.Parser
import java.util.*

/**
 * Exception thrown by the [Parser] implementations in case of malformed input.
 *
 * @param context Part of the input document in which vicinity the problem occurred.
 * @param contextMark Position of the `context` within the document.
 * @param problem Part of the input document that caused the problem.
 * @param problemMark Position of the `problem`. within the document.
 */
class ParserException @JvmOverloads constructor(
    problem: String?,
    context: String? = null,
    contextMark: Optional<Mark>,
    problemMark: Optional<Mark> = Optional.empty<Mark>(),
    cause: Throwable? = null,
) : MarkedYamlEngineException(
    context = context,
    contextMark = contextMark,
    problem = problem,
    problemMark = problemMark,
    cause = cause,
)
