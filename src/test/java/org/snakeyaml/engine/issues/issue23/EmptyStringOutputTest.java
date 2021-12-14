/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.issues.issue23;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
public class EmptyStringOutputTest {

    @Test
    void outputEmptyString() {
        Dump dumper = new Dump(DumpSettings.builder().build());
        String output = dumper.dumpToString("");
        assertEquals("''\n", output, "The output must NOT contain ---");
    }

    @Test
    void outputEmptyStringWithExplicitStart() {
        Dump dumper = new Dump(DumpSettings.builder().setExplicitStart(true).build());
        String output = dumper.dumpToString("");
        assertEquals("--- ''\n", output, "The output must contain ---");
    }

    @Test
    void outputEmptyStringWithEmitter() {
        assertEquals("---", dump(""), "The output must contain ---");
    }

    @Test
    void outputStringWithEmitter() {
        assertEquals("v1234512345", dump("v1234512345"), "The output must NOT contain ---");
    }

    private String dump(String value) {
        DumpSettings settings = DumpSettings.builder().build();
        MyWriter writer = new MyWriter();
        Emitter emitter = new Emitter(settings, writer);
        emitter.emit(new StreamStartEvent());
        emitter.emit(new DocumentStartEvent(false, Optional.empty(), new HashMap<>()));
        emitter.emit(new ScalarEvent(Optional.empty(), Optional.empty(), new ImplicitTuple(true, false), value, ScalarStyle.PLAIN, Optional.empty(), Optional.empty()));
        return writer.toString();
    }
}

class MyWriter extends StringWriter implements StreamDataWriter {
}
