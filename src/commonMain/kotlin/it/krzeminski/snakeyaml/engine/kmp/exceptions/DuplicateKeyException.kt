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

/**
 * Indicate duplicate keys in the same mapping during parsing
 *
 * @param contextMark - the context location
 * @param key - the data used as key more than once in the same mapping
 * @param problemMark - the problem location
 */
class DuplicateKeyException(
    contextMark: Mark?,
    key: Any,
    problemMark: Mark?,
) : ConstructorException(
    context = "while constructing a mapping",
    contextMark = contextMark,
    problem = "found duplicate key $key",
    problemMark = problemMark,
)
