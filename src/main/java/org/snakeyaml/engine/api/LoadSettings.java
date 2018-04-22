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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.snakeyaml.engine.nodes.Tag;
import org.snakeyaml.engine.resolver.JsonResolver;
import org.snakeyaml.engine.resolver.Resolver;


/**
 * Fine tuning parsing/loading
 */
public final class LoadSettings {
    private String label;
    private Optional<Tag> rootTag;
    private Resolver resolver;
    private Function<Integer, List> defaultList;
    private Function<Integer, Set> defaultSet;
    private Function<Integer, Map> defaultMap;
    private BiFunction<Integer, Integer, Boolean> yamlVersionReaction; //TODO should return Void instead of Boolean
    private Integer bufferSize;
    private Boolean allowDuplicateKeys;
    private Boolean useMarks;

    public LoadSettings() {
        this.label = "reader";
        this.rootTag = Optional.empty();
        this.resolver = new JsonResolver();
        this.defaultList = (initSize) -> new ArrayList(initSize);
        this.defaultSet = (initSize) -> new LinkedHashSet(initSize);
        this.defaultMap = (initSize) -> new LinkedHashMap(initSize);
        this.yamlVersionReaction = (major, minor) -> false;
        this.bufferSize = 1024;
        this.allowDuplicateKeys = false;
        this.useMarks = true;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Optional<Tag> getRootTag() {
        return rootTag;
    }

    public void setRootTag(Optional<Tag> rootTag) {
        this.rootTag = rootTag;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public void setResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    public Function<Integer, List> getDefaultList() {
        return defaultList;
    }

    public void setDefaultList(Function<Integer, List> defaultList) {
        this.defaultList = defaultList;
    }

    public Function<Integer, Set> getDefaultSet() {
        return defaultSet;
    }

    public void setDefaultSet(Function<Integer, Set> defaultSet) {
        this.defaultSet = defaultSet;
    }

    public Function<Integer, Map> getDefaultMap() {
        return defaultMap;
    }

    public void setDefaultMap(Function<Integer, Map> defaultMap) {
        this.defaultMap = defaultMap;
    }

    public BiFunction<Integer, Integer, Boolean> getYamlVersionReaction() {
        return yamlVersionReaction;
    }

    public void setYamlVersionReaction(BiFunction<Integer, Integer, Boolean> yamlVersionReaction) {
        this.yamlVersionReaction = yamlVersionReaction;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Boolean getAllowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    public void setAllowDuplicateKeys(Boolean allowDuplicateKeys) {
        this.allowDuplicateKeys = allowDuplicateKeys;
    }

    public Boolean getUseMarks() {
        return useMarks;
    }

    public void setUseMarks(Boolean useMarks) {
        this.useMarks = useMarks;
    }
}

