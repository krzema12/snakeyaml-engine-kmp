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

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import org.snakeyaml.engine.composer.Composer;
import org.snakeyaml.engine.constructor.StandardConstructor;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.parser.ParserImpl;
import org.snakeyaml.engine.scanner.StreamReader;

/**
 * Common way to load Java instance(s)
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

    private Composer createComposer(LoadSettings settings, StreamReader streamReader) {
        return new Composer(new ParserImpl(streamReader, settings), settings.getScalarResolver());
    }

    // Load  a single document

    private Object loadOne(Composer composer) {
        Optional<Node> nodeOptional = composer.getSingleNode();
        StandardConstructor constructor = new StandardConstructor(settings);
        return constructor.constructSingleDocument(nodeOptional);
    }

    /**
     * Parse the only YAML document in a stream and produce the corresponding
     * Java object.
     *
     * @param yamlStream - data to load from (BOM is respected to detect encoding and removed from the data)
     * @return parsed Java instance
     */
    public Object loadFromInputStream(InputStream yamlStream) {
        Objects.requireNonNull(yamlStream, "InputStream cannot be null");
        return loadOne(createComposer(settings, new StreamReader(new YamlUnicodeReader(yamlStream), settings)));
    }

    /**
     * Parse a YAML document and create a Java instance
     *
     * @param yamlReader - data to load from (BOM must not be present)
     * @return parsed Java instance
     */
    public Object loadFromReader(Reader yamlReader) {
        Objects.requireNonNull(yamlReader, "Reader cannot be null");
        return loadOne(createComposer(settings, new StreamReader(yamlReader, settings)));
    }

    /**
     * Parse a YAML document and create a Java instance
     *
     * @param yaml - YAML data to load from (BOM must not be present)
     * @return parsed Java instance
     * @throws org.snakeyaml.engine.exceptions.YamlEngineException if the YAML is not valid
     */
    public Object loadFromString(String yaml) {
        Objects.requireNonNull(yaml, "String cannot be null");
        return loadOne(createComposer(settings, new StreamReader(yaml, settings)));
    }

    // Load all the documents

    private Iterable<Object> loadAll(Composer composer) {
        StandardConstructor constructor = new StandardConstructor(settings);
        Iterator<Object> result = new YamlIterator(composer, constructor);
        return new YamlIterable(result);
    }

    /**
     * Parse all YAML documents in a stream and produce corresponding Java
     * objects. The documents are parsed only when the iterator is invoked.
     *
     * @param yamlStream - YAML data to load from (BOM is respected to detect encoding and removed from the data)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    public Iterable<Object> loadAllFromInputStream(InputStream yamlStream) {
        Objects.requireNonNull(yamlStream, "InputStream cannot be null");
        Composer composer = createComposer(settings, new StreamReader(new YamlUnicodeReader(yamlStream), settings));
        return loadAll(composer);
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java
     * objects. The documents are parsed only when the iterator is invoked.
     * @param yamlReader - YAML data to load from (BOM must not be present)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    public Iterable<Object> loadAllFromReader(Reader yamlReader) {
        Objects.requireNonNull(yamlReader, "Reader cannot be null");
        Composer composer = createComposer(settings, new StreamReader(yamlReader, settings));
        return loadAll(composer);
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java
     * objects. (Because the encoding in known BOM is not respected.) The
     * documents are parsed only when the iterator is invoked.
     * @param yaml - YAML data to load from (BOM must not be present)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    public Iterable<Object> loadAllFromString(String yaml) {
        Objects.requireNonNull(yaml, "String cannot be null");
        Composer composer = createComposer(settings, new StreamReader(yaml, settings));
        return loadAll(composer);
    }

    private static class YamlIterable implements Iterable<Object> {
        private Iterator<Object> iterator;

        public YamlIterable(Iterator<Object> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<Object> iterator() {
            return iterator;
        }
    }

    private static class YamlIterator implements Iterator<Object> {
        private Composer composer;
        private StandardConstructor constructor;

        public YamlIterator(Composer composer, StandardConstructor constructor) {
            this.composer = composer;
            this.constructor = constructor;
        }

        @Override
        public boolean hasNext() {
            return composer.hasNext();
        }

        @Override
        public Object next() {
            Node node = composer.next();
            return constructor.constructSingleDocument(Optional.of(node));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Removing is not supported.");
        }
    }
}




