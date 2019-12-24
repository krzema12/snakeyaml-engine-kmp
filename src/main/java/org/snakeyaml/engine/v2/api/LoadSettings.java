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
import org.snakeyaml.engine.v2.env.EnvConfig;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

/**
 * Fine tuning parsing/loading
 * Description for all the fields can be found in the builder
 */
public final class LoadSettings {
    private final String label;
    private final Map<Tag, ConstructNode> tagConstructors;
    private final ScalarResolver scalarResolver;
    private final IntFunction<List> defaultList;
    private final IntFunction<Set> defaultSet;
    private final IntFunction<Map> defaultMap;
    private final UnaryOperator<SpecVersion> versionFunction;
    private final Integer bufferSize;
    private final boolean allowDuplicateKeys;
    private final boolean allowRecursiveKeys;
    private final int maxAliasesForCollections;
    private final boolean useMarks;
    private final Optional<EnvConfig> envConfig;

    //general
    private final Map<SettingKey, Object> customProperties;

    LoadSettings(String label, Map<Tag, ConstructNode> tagConstructors,
                 ScalarResolver scalarResolver, IntFunction<List> defaultList,
                 IntFunction<Set> defaultSet, IntFunction<Map> defaultMap,
                 UnaryOperator<SpecVersion> versionFunction, Integer bufferSize,
                 boolean allowDuplicateKeys, boolean allowRecursiveKeys, int maxAliasesForCollections,
                 boolean useMarks, Map<SettingKey, Object> customProperties, Optional<EnvConfig> envConfig) {
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
        this.maxAliasesForCollections = maxAliasesForCollections;
        this.useMarks = useMarks;
        this.customProperties = customProperties;
        this.envConfig = envConfig;
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

    public IntFunction<List> getDefaultList() {
        return defaultList;
    }

    public IntFunction<Set> getDefaultSet() {
        return defaultSet;
    }

    public IntFunction<Map> getDefaultMap() {
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

    public int getMaxAliasesForCollections() {
        return maxAliasesForCollections;
    }

    public Optional<EnvConfig> getEnvConfig() {
        return envConfig;
    }
}

