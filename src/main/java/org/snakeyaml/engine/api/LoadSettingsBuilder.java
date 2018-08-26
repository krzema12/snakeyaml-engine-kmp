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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.snakeyaml.engine.common.SpecVersion;
import org.snakeyaml.engine.exceptions.YamlVersionException;
import org.snakeyaml.engine.nodes.Tag;
import org.snakeyaml.engine.resolver.JsonScalarResolver;
import org.snakeyaml.engine.resolver.ScalarResolver;


/**
 * Fine tuning parsing/loading
 * TODO add JavaDoc for all methods
 */
public final class LoadSettingsBuilder {
    private String label;
    private Optional<ConstructNode> rootConstructNode;
    private Map<Tag, ConstructNode> tagConstructors;
    private ScalarResolver scalarResolver;
    private Function<Integer, List> defaultList;
    private Function<Integer, Set> defaultSet;
    private Function<Integer, Map> defaultMap;
    private Function<SpecVersion, SpecVersion> versionFunction;
    private Integer bufferSize;
    private boolean allowDuplicateKeys;
    private boolean useMarks;

    public LoadSettingsBuilder() {
        this.label = "reader";
        this.rootConstructNode = Optional.empty();
        this.tagConstructors = new HashMap<>();
        this.scalarResolver = new JsonScalarResolver();
        this.defaultList = (initSize) -> new ArrayList(initSize);
        this.defaultSet = (initSize) -> new LinkedHashSet(initSize);
        this.defaultMap = (initSize) -> new LinkedHashMap(initSize);
        this.versionFunction = (version) -> {
            if (version.getMajor() != 1) throw new YamlVersionException(version);
            return version;
        };
        this.bufferSize = 1024;
        this.allowDuplicateKeys = false;
        this.useMarks = true;
    }

    public LoadSettingsBuilder setLabel(String label) {
        Objects.requireNonNull(label, "label cannot be null");
        this.label = label;
        return this;
    }

    public LoadSettingsBuilder setRootConstructNode(Optional<ConstructNode> rootConstructNode) {
        Objects.requireNonNull(rootConstructNode, "rootConstructNode cannot be null");
        this.rootConstructNode = rootConstructNode;
        return this;
    }

    public LoadSettingsBuilder setTagConstructors(Map<Tag, ConstructNode> tagConstructors) {
        this.tagConstructors = tagConstructors;
        return this;
    }

    public LoadSettingsBuilder setScalarResolver(ScalarResolver scalarResolver) {
        Objects.requireNonNull(scalarResolver, "scalarResolver cannot be null");
        this.scalarResolver = scalarResolver;
        return this;
    }

    public LoadSettingsBuilder setDefaultList(Function<Integer, List> defaultList) {
        Objects.requireNonNull(defaultList, "defaultList cannot be null");
        this.defaultList = defaultList;
        return this;
    }

    public LoadSettingsBuilder setDefaultSet(Function<Integer, Set> defaultSet) {
        Objects.requireNonNull(defaultSet, "defaultSet cannot be null");
        this.defaultSet = defaultSet;
        return this;
    }

    public LoadSettingsBuilder setDefaultMap(Function<Integer, Map> defaultMap) {
        Objects.requireNonNull(defaultMap, "defaultMap cannot be null");
        this.defaultMap = defaultMap;
        return this;
    }

    public LoadSettingsBuilder setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public LoadSettingsBuilder setAllowDuplicateKeys(boolean allowDuplicateKeys) {
        this.allowDuplicateKeys = allowDuplicateKeys;
        return this;
    }

    public LoadSettingsBuilder setUseMarks(boolean useMarks) {
        this.useMarks = useMarks;
        return this;
    }

    public LoadSettingsBuilder setVersionFunction(Function<SpecVersion, SpecVersion> versionFunction) {
        Objects.requireNonNull(versionFunction, "versionFunction cannot be null");
        this.versionFunction = versionFunction;
        return this;
    }

    public LoadSettings build() {
        return new LoadSettings(label, rootConstructNode, tagConstructors,
                scalarResolver, defaultList,
                defaultSet, defaultMap,
                versionFunction, bufferSize,
                allowDuplicateKeys, useMarks);
    }
}

