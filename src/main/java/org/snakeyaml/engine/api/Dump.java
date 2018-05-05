/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.api;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Objects;

import org.snakeyaml.engine.emitter.Emitter;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.representer.StandardRepresenter;
import org.snakeyaml.engine.serializer.Serializer;

public class Dump {

    private DumpSettings settings;

    /**
     * @param settings - configuration
     */
    public Dump(DumpSettings settings) {
        Objects.requireNonNull(settings, "DumpSettings cannot be null");
        this.settings = settings;
    }

    public String dumpToString(Object yaml) {
        StandardRepresenter representer = new StandardRepresenter();
        StreamToString writer = new StreamToString();
        Serializer serializer = new Serializer(settings, new Emitter(settings, writer));
        serializer.open();
        Node node = representer.represent(yaml);
        serializer.serialize(node);
        serializer.close();
        return writer.toString();
    }

    public void dump(Object yaml, StreamDataWriter streamDataWriter) {
        StandardRepresenter representer = new StandardRepresenter();
        Serializer serializer = new Serializer(settings, new Emitter(settings, streamDataWriter));
        Node node = representer.represent(yaml);
        serializer.open();
        serializer.serialize(node);
        serializer.close();
    }

    public void dumpAll(Iterator<Object> instancesIterator, StreamDataWriter streamDataWriter) {
        StandardRepresenter representer = new StandardRepresenter();
        Serializer serializer = new Serializer(settings, new Emitter(settings, streamDataWriter));
        serializer.open();
        while (instancesIterator.hasNext()) {
            Object instance = instancesIterator.next();
            Node node = representer.represent(instance);
            serializer.serialize(node);
        }
        serializer.close();
    }
}

class StreamToString extends StringWriter implements StreamDataWriter {
}




