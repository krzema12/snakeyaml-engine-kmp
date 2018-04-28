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

import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.serializer.Serializer;

public class Serialize {

    private DumpSettings settings;

    /**
     * Create
     *
     * @param settings - configuration
     */
    public Serialize(DumpSettings settings) {
        this.settings = settings;
    }

    public List<Event> serialize(Node node) {
        Serializer serializer = new Serializer(settings);
        return serializer.serialize(node);
    }
}

