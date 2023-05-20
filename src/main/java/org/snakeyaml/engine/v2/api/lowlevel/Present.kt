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
package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.events.Event
import java.io.StringWriter

/**
 * Emit the events into a data stream (opposite for Parse)
 *
 * @param settings - configuration
 */
class Present(
    private val settings: DumpSettings,
) {
    /**
     * Serialise the provided Events
     *
     * @param events - the data to serialise
     * @return - the YAML document
     */
    fun emitToString(events: Iterator<Event>): String {
        val writer = StreamToStringWriter()
        val emitter = Emitter(settings, writer)
        events.forEachRemaining { event: Event -> emitter.emit(event) }
        return writer.toString()
    }
}

/**
 * Internal helper class to support emitting to String
 */
private class StreamToStringWriter : StringWriter(), StreamDataWriter {
    override fun flush(): Unit = super<StringWriter>.flush()
}
