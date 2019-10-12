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
package org.snakeyaml.engine.v2.api;

import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Fine tuning parsing/loading
 * Description for all the fields can be found in the builder
 */
public final class LoadSettings {
    private final String label;
    private final Map<Tag, ConstructNode> tagConstructors;
    private final ScalarResolver scalarResolver;
    private final Function<Integer, List> defaultList;
    private final Function<Integer, Set> defaultSet;
    private final Function<Integer, Map> defaultMap;
    private final Function<SpecVersion, SpecVersion> versionFunction;
    private final Integer bufferSize;
    private final boolean allowDuplicateKeys;
    private final boolean allowRecursiveKeys;
    private final boolean useMarks;

    //general
    private final Map<SettingKey, Object> customProperties;

    LoadSettings(String label, Map<Tag, ConstructNode> tagConstructors,
                 ScalarResolver scalarResolver, Function<Integer, List> defaultList,
                 Function<Integer, Set> defaultSet, Function<Integer, Map> defaultMap,
                 Function<SpecVersion, SpecVersion> versionFunction, Integer bufferSize,
                 boolean allowDuplicateKeys, boolean allowRecursiveKeys, boolean useMarks,
                 Map<SettingKey, Object> customProperties) {
        this.label = label;
        this.tagConstructors = tagConstructors;
        this.scalarResolver = scalarResolver;
        this.defaultList = defaultList;
        this.defaultSet = defaultSet;
        this.defaultMap = defaultMap;
        this.versionFunction = versionFunction;
        this.bufferSize = bufferSize;
        this.allowDuplicateKeys = allowDuplicateKeys;
        this.allowRecursiveKeys = allowRecursiveKeys;
        this.useMarks = useMarks;
        this.customProperties = customProperties;
    }

    public static final LoadSettingsBuilder builder() {
        return new LoadSettingsBuilder();
    }

    public String getLabel() {
        return label;
    }

    public Map<Tag, ConstructNode> getTagConstructors() {
        return tagConstructors;
    }

    public ScalarResolver getScalarResolver() {
        return scalarResolver;
    }

    public Function<Integer, List> getDefaultList() {
        return defaultList;
    }

    public Function<Integer, Set> getDefaultSet() {
        return defaultSet;
    }

    public Function<Integer, Map> getDefaultMap() {
        return defaultMap;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public boolean getAllowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    public boolean getAllowRecursiveKeys() {
        return allowRecursiveKeys;
    }

    public boolean getUseMarks() {
        return useMarks;
    }

    public Function<SpecVersion, SpecVersion> getVersionFunction() {
        return versionFunction;
    }

    public Object getCustomProperty(SettingKey key) {
        return customProperties.get(key);
    }
}

