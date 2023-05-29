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
 * General exception to serve as the root
 */
open class YamlEngineException : RuntimeException {
    /**
     * @param message - the problem
     */
    constructor(message: String) : super(message)

    /**
     * @param cause - the reason
     */
    constructor(cause: Throwable) : super(cause)

    /**
     * @param message - error
     * @param cause - the cause
     */
    constructor(message: String, cause: Throwable?) : super(message, cause)
}
