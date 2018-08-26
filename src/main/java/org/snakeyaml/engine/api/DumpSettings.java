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

import java.util.Map;
import java.util.Optional;

import org.snakeyaml.engine.common.FlowStyle;
import org.snakeyaml.engine.common.ScalarStyle;
import org.snakeyaml.engine.common.SpecVersion;
import org.snakeyaml.engine.nodes.Tag;
import org.snakeyaml.engine.resolver.ScalarResolver;
import org.snakeyaml.engine.serializer.AnchorGenerator;

/**
 * Fine tuning serializing/dumping
 * TODO add JavaDoc for all methods
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


    DumpSettings(String label, boolean explicitStart, boolean explicitEnd, Optional<Tag> explicitRootTag,
                 AnchorGenerator anchorGenerator, Optional<SpecVersion> specVersion, Map<String, String> useTags,
                 ScalarResolver scalarResolver, FlowStyle defaultFlowStyle, ScalarStyle defaultScalarStyle,
                 //emitter
                 Optional<Tag> rootTag, boolean canonical, boolean prettyFlow, boolean useUnicodeEncoding,
                 int indent, int indicatorIndent, int width, String bestLineBreak, boolean splitLines
    ) {
        this.label = label;
        this.explicitStart = explicitStart;
        this.explicitEnd = explicitEnd;
        this.explicitRootTag = explicitRootTag;
        this.anchorGenerator = anchorGenerator;
        this.specVersion = specVersion;
        this.useTags = useTags;
        this.scalarResolver = scalarResolver;
        this.defaultFlowStyle = defaultFlowStyle;
        this.defaultScalarStyle = defaultScalarStyle;
        this.rootTag = rootTag;
        this.canonical = canonical;
        this.prettyFlow = prettyFlow;
        this.useUnicodeEncoding = useUnicodeEncoding;
        this.indent = indent;
        this.indicatorIndent = indicatorIndent;
        this.width = width;
        this.bestLineBreak = bestLineBreak;
        this.splitLines = splitLines;
    }


    public FlowStyle getDefaultFlowStyle() {
        return defaultFlowStyle;
    }

    public ScalarStyle getDefaultScalarStyle() {
        return defaultScalarStyle;
    }

    public String getLabel() {
        return label;
    }

    public boolean isExplicitStart() {
        return explicitStart;
    }

    public AnchorGenerator getAnchorGenerator() {
        return anchorGenerator;
    }

    public ScalarResolver getScalarResolver() {
        return scalarResolver;
    }

    public boolean isExplicitEnd() {
        return explicitEnd;
    }

    public Optional<Tag> getExplicitRootTag() {
        return explicitRootTag;
    }

    public Optional<SpecVersion> getSpecVersion() {
        return specVersion;
    }

    public Map<String, String> getUseTags() {
        return useTags;
    }

    public Optional<Tag> getRootTag() {
        return rootTag;
    }

    public boolean isCanonical() {
        return canonical;
    }

    public boolean isPrettyFlow() {
        return prettyFlow;
    }

    public boolean isUseUnicodeEncoding() {
        return useUnicodeEncoding;
    }

    public int getIndent() {
        return indent;
    }

    public int getIndicatorIndent() {
        return indicatorIndent;
    }

    public int getWidth() {
        return width;
    }

    public String getBestLineBreak() {
        return bestLineBreak;
    }

    public boolean isSplitLines() {
        return splitLines;
    }

}

