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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.snakeyaml.engine.common.Anchor;
import org.snakeyaml.engine.common.SpecVersion;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.nodes.Tag;
import org.snakeyaml.engine.resolver.JsonScalarResolver;
import org.snakeyaml.engine.resolver.ScalarResolver;
import org.snakeyaml.engine.serializer.AnchorGenerator;
import org.snakeyaml.engine.serializer.NumberAnchorGenerator;

/**
 * Fine tuning serializing/dumping
 */
public final class DumpSettings {
    private String label;
    private boolean explicitStart;
    private boolean explicitEnd;
    private Optional<Tag> explicitRootTag;
    private AnchorGenerator anchorGenerator;
    private SpecVersion specVersion;
    private Map<String, String> useTags;
    private ScalarResolver scalarResolver;


    public DumpSettings() {
        this.label = "reader";
        this.explicitRootTag = Optional.empty();
        this.useTags = new HashMap<>();
        this.scalarResolver = new JsonScalarResolver();
        this.anchorGenerator = new NumberAnchorGenerator(1);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isExplicitStart() {
        return explicitStart;
    }

    public void setExplicitStart(boolean explicitStart) {
        this.explicitStart = explicitStart;
    }

    public AnchorGenerator getAnchorGenerator() {
        return anchorGenerator;
    }

    public void setAnchorGenerator(AnchorGenerator anchorGenerator) {
        this.anchorGenerator = anchorGenerator;
    }

    public ScalarResolver getScalarResolver() {
        return scalarResolver;
    }

    public void setScalarResolver(ScalarResolver scalarResolver) {
        this.scalarResolver = scalarResolver;
    }

    public boolean isExplicitEnd() {
        return explicitEnd;
    }

    public Optional<Tag> getExplicitRootTag() {
        return explicitRootTag;
    }

    public void setExplicitRootTag(Optional<Tag> explicitRootTag) {
        this.explicitRootTag = explicitRootTag;
    }

    public void setExplicitEnd(boolean explicitEnd) {
        this.explicitEnd = explicitEnd;
    }

    public SpecVersion getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(SpecVersion specVersion) {
        this.specVersion = specVersion;
    }

    public Map<String, String> getUseTags() {
        return useTags;
    }

    public void setUseTags(Map<String, String> useTags) {
        this.useTags = useTags;
    }
}

