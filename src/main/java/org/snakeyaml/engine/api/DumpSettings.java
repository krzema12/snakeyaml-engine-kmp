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
 */
public final class DumpSettings {
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


    public DumpSettings() {
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

    public FlowStyle getDefaultFlowStyle() {
        return defaultFlowStyle;
    }

    public void setDefaultFlowStyle(FlowStyle defaultFlowStyle) {
        this.defaultFlowStyle = defaultFlowStyle;
    }

    public ScalarStyle getDefaultScalarStyle() {
        return defaultScalarStyle;
    }

    public void setDefaultScalarStyle(ScalarStyle defaultScalarStyle) {
        this.defaultScalarStyle = defaultScalarStyle;
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
        Objects.requireNonNull(anchorGenerator, "anchorGenerator cannot be null");
        this.anchorGenerator = anchorGenerator;
    }

    public ScalarResolver getScalarResolver() {
        return scalarResolver;
    }

    public void setScalarResolver(ScalarResolver scalarResolver) {
        Objects.requireNonNull(scalarResolver, "scalarResolver cannot be null");
        this.scalarResolver = scalarResolver;
    }

    public boolean isExplicitEnd() {
        return explicitEnd;
    }

    public Optional<Tag> getExplicitRootTag() {
        return explicitRootTag;
    }

    public void setExplicitRootTag(Optional<Tag> explicitRootTag) {
        Objects.requireNonNull(explicitRootTag, "explicitRootTag cannot be null");
        this.explicitRootTag = explicitRootTag;
    }

    public void setExplicitEnd(boolean explicitEnd) {
        this.explicitEnd = explicitEnd;
    }

    public Optional<SpecVersion> getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(Optional<SpecVersion> specVersion) {
        Objects.requireNonNull(specVersion, "specVersion cannot be null");
        this.specVersion = specVersion;
    }

    public Map<String, String> getUseTags() {
        return useTags;
    }

    public void setUseTags(Map<String, String> useTags) {
        Objects.requireNonNull(useTags, "useTags cannot be null");
        this.useTags = useTags;
    }

    public Optional<Tag> getRootTag() {
        return rootTag;
    }

    public void setRootTag(Optional<Tag> rootTag) {
        Objects.requireNonNull(rootTag, "rootTag cannot be null");
        this.rootTag = rootTag;
    }

    public boolean isCanonical() {
        return canonical;
    }

    public void setCanonical(boolean canonical) {
        this.canonical = canonical;
    }

    public boolean isPrettyFlow() {
        return prettyFlow;
    }

    public void setPrettyFlow(boolean prettyFlow) {
        this.prettyFlow = prettyFlow;
    }

    public boolean isUseUnicodeEncoding() {
        return useUnicodeEncoding;
    }

    public void setUseUnicodeEncoding(boolean useUnicodeEncoding) {
        this.useUnicodeEncoding = useUnicodeEncoding;
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public int getIndicatorIndent() {
        return indicatorIndent;
    }

    public void setIndicatorIndent(int indicatorIndent) {
        this.indicatorIndent = indicatorIndent;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getBestLineBreak() {
        return bestLineBreak;
    }

    public void setBestLineBreak(String bestLineBreak) {
        Objects.requireNonNull(bestLineBreak, "bestLineBreak cannot be null");
        this.bestLineBreak = bestLineBreak;
    }

    public boolean isSplitLines() {
        return splitLines;
    }

    public void setSplitLines(boolean splitLines) {
        this.splitLines = splitLines;
    }
}

