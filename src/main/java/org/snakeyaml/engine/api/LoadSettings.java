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
import java.util.function.Function;

import org.snakeyaml.engine.common.SpecVersion;
import org.snakeyaml.engine.exceptions.YamlVersionException;
import org.snakeyaml.engine.nodes.Tag;
import org.snakeyaml.engine.resolver.JsonScalarResolver;
import org.snakeyaml.engine.resolver.ScalarResolver;


/**
 * Fine tuning parsing/loading
 */
public final class LoadSettings {
    private String label;
    private Optional<Tag> rootTag;
    private ScalarResolver scalarResolver;
    private Function<Integer, List> defaultList;
    private Function<Integer, Set> defaultSet;
    private Function<Integer, Map> defaultMap;
    private Function<SpecVersion, SpecVersion> versionFunction;
    private Integer bufferSize;
    private Boolean allowDuplicateKeys;
    private Boolean useMarks;

    public LoadSettings() {
        this.label = "reader";
        this.rootTag = Optional.empty();
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

    public ScalarResolver getScalarResolver() {
        return scalarResolver;
    }

    public void setScalarResolver(ScalarResolver scalarResolver) {
        this.scalarResolver = scalarResolver;
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

    public Function<SpecVersion, SpecVersion> getVersionFunction() {
        return versionFunction;
    }

    public void setVersionFunction(Function<SpecVersion, SpecVersion> versionFunction) {
        this.versionFunction = versionFunction;
    }
}

