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

import java.util.Objects;
import java.util.Optional;

import org.snakeyaml.engine.composer.Composer;
import org.snakeyaml.engine.constructor.StandardConstructor;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.parser.ParserImpl;
import org.snakeyaml.engine.scanner.StreamReader;

/**
 * Common way to load any Java instance(s)
 */
public class Load {

    private LoadSettings settings;

    /**
     * Create instance to parse the incoming YAML data and create Java instances
     *
     * @param settings - configuration
     */
    public Load(LoadSettings settings) {
        Objects.requireNonNull(settings, "LoadSettings cannot be null");
        this.settings = settings;
    }

    /**
     * Parse a YAML document and create a Java instance
     * @param yaml - YAML document
     * @return parsed Java instance
     * @throws org.snakeyaml.engine.exceptions.YamlEngineException if the YAML is not valid
     */
    public Object loadFromString(String yaml) {
        Objects.requireNonNull(yaml, "String cannot be null");
        Optional<Node> nodeOptional = new Composer(new ParserImpl(new StreamReader(
                yaml, settings), settings), settings.getScalarResolver()).getSingleNode();
        StandardConstructor constructor = new StandardConstructor(settings);
        return constructor.constructSingleDocument(nodeOptional);
    }

    //TODO load from InputStream, from Reader
    //TODO load iterator

}




