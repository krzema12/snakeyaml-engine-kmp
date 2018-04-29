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

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.snakeyaml.engine.nodes.Node;

public class Load {

    private LoadSettings settings;

    /**
     * @param settings - configuration
     */
    public Load(LoadSettings settings) {
        Objects.requireNonNull(settings, "LoadSettings cannot be null");
        this.settings = settings;
    }

    public Object load(Reader yaml) {
        Objects.requireNonNull(yaml, "Reader cannot be null");
        return null;
    }

    public List<Object> loadAll(Reader yaml) {
        Objects.requireNonNull(yaml, "Reader cannot be null");
        return Collections.emptyList();
    }
}




