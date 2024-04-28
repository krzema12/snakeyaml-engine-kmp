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
package it.krzeminski.snakeyaml.engine.kmp.exceptions

import kotlin.jvm.JvmOverloads

/**
 * General exception during construction step
 * @param cause - the reason
 * @param context - part of the document
 * @param contextMark - context position
 * @param problem - the issue
 * @param problemMark - problem position
 */
open class ConstructorException @JvmOverloads constructor(
    context: String?,
    contextMark: Mark?,
    problem: String,
    problemMark: Mark?,
    cause: Throwable? = null,
) : MarkedYamlEngineException(
    context = context,
    contextMark = contextMark,
    problem = problem,
    problemMark = problemMark,
    cause = cause,
)
