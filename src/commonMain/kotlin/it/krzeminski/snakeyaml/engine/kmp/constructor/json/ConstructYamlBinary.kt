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
package it.krzeminski.snakeyaml.engine.kmp.constructor.json

import okio.ByteString.Companion.decodeBase64
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructScalar
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Create instances bytes for binary
 */
class ConstructYamlBinary : ConstructScalar() {
    override fun construct(node: Node?): ByteArray {
        // Ignore white spaces for base64 encoded scalar
        // TODO decodeBase64() doesn't seem to require removing whitespace - perhaps this can be removed?
        //      I'm leaving it in because I'm not confident about edge case test coverage for whitespaces.
        val noWhiteSpaces = constructScalar(node).replace(SPACES_PATTERN, "")
        return noWhiteSpaces.decodeBase64()?.toByteArray() ?: byteArrayOf()
    }

    companion object {
        private val SPACES_PATTERN = Regex("""\s""")
    }
}
