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
package it.krzeminski.snakeyaml.engine.kmp.api

import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

/**
 * Provide an example of implementation of [StreamDataWriter] interface which does not throw [IOException]
 *
 * @param out the output
 * @param cs encoding to use to translate String to bytes
 */
abstract class YamlOutputStreamWriter(
    out: OutputStream,
    cs: Charset,
) : OutputStreamWriter(out, cs), StreamDataWriter {
    /**
     * to be implemented
     *
     * @param e - the reason
     */
    abstract fun processIOException(e: IOException?)

    override fun flush() {
        try {
            super<StreamDataWriter>.flush()
        } catch (e: IOException) {
            processIOException(e)
        }
    }

    override fun write(str: String, off: Int, len: Int) {
        try {
            super.write(str, off, len)
        } catch (e: IOException) {
            processIOException(e)
        }
    }

    override fun write(str: String) {
        try {
            super.write(str)
        } catch (e: IOException) {
            processIOException(e)
        }
    }
}
