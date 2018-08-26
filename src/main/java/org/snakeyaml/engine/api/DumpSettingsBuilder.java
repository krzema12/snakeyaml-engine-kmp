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
import java.util.Objects;
import java.util.Optional;

import org.snakeyaml.engine.common.FlowStyle;
import org.snakeyaml.engine.common.ScalarStyle;
import org.snakeyaml.engine.common.SpecVersion;
import org.snakeyaml.engine.nodes.Tag;
import org.snakeyaml.engine.resolver.JsonScalarResolver;
import org.snakeyaml.engine.resolver.ScalarResolver;
import org.snakeyaml.engine.serializer.AnchorGenerator;
import org.snakeyaml.engine.serializer.NumberAnchorGenerator;

/**
 * Fine tuning serializing/dumping
 * TODO add JavaDoc for all methods
 */
public final class DumpSettingsBuilder {
    private String label;
    private boolean explicitStart;
    private boolean explicitEnd;
    private Optional<Tag> explicitRootTag;
    private AnchorGenerator anchorGenerator;
    private Optional<SpecVersion> specVersion;
    private Map<String, String> useTags;
    private ScalarResolver scalarResolver;
    private FlowStyle defaultFlowStyle;
    private ScalarStyle defaultScalarStyle;

    //emitter
    private Optional<Tag> rootTag;
    private boolean canonical;
    private boolean prettyFlow;
    private boolean useUnicodeEncoding;
    private int indent;
    private int indicatorIndent;
    private int width;
    private String bestLineBreak;
    private boolean splitLines;


    public DumpSettingsBuilder() {
        this.label = "reader";
        this.explicitRootTag = Optional.empty();
        this.useTags = new HashMap<>();
        this.scalarResolver = new JsonScalarResolver();
        this.anchorGenerator = new NumberAnchorGenerator(1);
        this.bestLineBreak = "\n";
        this.canonical = false;
        this.useUnicodeEncoding = true;
        this.indent = 2;
        this.indicatorIndent = 0;
        this.width = 80;
        this.splitLines = true;
        this.explicitStart = false;
        this.explicitEnd = false;
        this.specVersion = Optional.empty();
        this.defaultFlowStyle = FlowStyle.AUTO;
        this.defaultScalarStyle = ScalarStyle.PLAIN;
    }

    public DumpSettingsBuilder setDefaultFlowStyle(FlowStyle defaultFlowStyle) {
        this.defaultFlowStyle = defaultFlowStyle;
        return this;
    }

    public DumpSettingsBuilder setDefaultScalarStyle(ScalarStyle defaultScalarStyle) {
        this.defaultScalarStyle = defaultScalarStyle;
        return this;
    }

    public DumpSettingsBuilder setLabel(String label) {
        Objects.requireNonNull(label, "label cannot be null");
        this.label = label;
        return this;
    }

    public DumpSettingsBuilder setExplicitStart(boolean explicitStart) {
        this.explicitStart = explicitStart;
        return this;
    }

    public DumpSettingsBuilder setAnchorGenerator(AnchorGenerator anchorGenerator) {
        Objects.requireNonNull(anchorGenerator, "anchorGenerator cannot be null");
        this.anchorGenerator = anchorGenerator;
        return this;
    }

    public DumpSettingsBuilder setScalarResolver(ScalarResolver scalarResolver) {
        Objects.requireNonNull(scalarResolver, "scalarResolver cannot be null");
        this.scalarResolver = scalarResolver;
        return this;
    }

    public DumpSettingsBuilder setExplicitRootTag(Optional<Tag> explicitRootTag) {
        Objects.requireNonNull(explicitRootTag, "explicitRootTag cannot be null");
        this.explicitRootTag = explicitRootTag;
        return this;
    }

    public DumpSettingsBuilder setExplicitEnd(boolean explicitEnd) {
        this.explicitEnd = explicitEnd;
        return this;
    }

    public DumpSettingsBuilder setSpecVersion(Optional<SpecVersion> specVersion) {
        Objects.requireNonNull(specVersion, "specVersion cannot be null");
        this.specVersion = specVersion;
        return this;
    }

    public DumpSettingsBuilder setUseTags(Map<String, String> useTags) {
        Objects.requireNonNull(useTags, "useTags cannot be null");
        this.useTags = useTags;
        return this;
    }

    public DumpSettingsBuilder setRootTag(Optional<Tag> rootTag) {
        Objects.requireNonNull(rootTag, "rootTag cannot be null");
        this.rootTag = rootTag;
        return this;
    }

    public DumpSettingsBuilder setCanonical(boolean canonical) {
        this.canonical = canonical;
        return this;
    }

    public DumpSettingsBuilder setPrettyFlow(boolean prettyFlow) {
        this.prettyFlow = prettyFlow;
        return this;
    }

    public DumpSettingsBuilder setUseUnicodeEncoding(boolean useUnicodeEncoding) {
        this.useUnicodeEncoding = useUnicodeEncoding;
        return this;
    }

    public DumpSettingsBuilder setIndent(int indent) {
        this.indent = indent;
        return this;
    }

    public DumpSettingsBuilder setIndicatorIndent(int indicatorIndent) {
        this.indicatorIndent = indicatorIndent;
        return this;
    }

    public DumpSettingsBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public DumpSettingsBuilder setBestLineBreak(String bestLineBreak) {
        Objects.requireNonNull(bestLineBreak, "bestLineBreak cannot be null");
        this.bestLineBreak = bestLineBreak;
        return this;
    }

    public DumpSettingsBuilder setSplitLines(boolean splitLines) {
        this.splitLines = splitLines;
        return this;
    }

    public DumpSettings build() {
        return new DumpSettings(label, explicitStart, explicitEnd, explicitRootTag,
                anchorGenerator, specVersion, useTags,
                scalarResolver, defaultFlowStyle, defaultScalarStyle,
                //emitter
                rootTag, canonical, prettyFlow, useUnicodeEncoding,
                indent, indicatorIndent, width, bestLineBreak, splitLines);
    }
}

