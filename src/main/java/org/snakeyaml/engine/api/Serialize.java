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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.serializer.Serializer;

public class Serialize {

    private final DumpSettings settings;

    /**
     * Create
     *
     * @param settings - configuration
     */
    public Serialize(DumpSettings settings) {
        Objects.requireNonNull(settings, "DumpSettings cannot be null");
        this.settings = settings;
    }

    //TODO iterator
    public List<Event> serializeOne(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        return serializeAll(Collections.singletonList(node));
    }

    //TODO iterator
    public List<Event> serializeAll(List<Node> nodes) {
        Objects.requireNonNull(nodes, "Nodes cannot be null");
        Serializer serializer = new Serializer(settings);
        serializer.open();
        for (Node node : nodes) {
            serializer.serialize(node);
        }
        serializer.close();
        return serializer.getEmitter();
    }
}

