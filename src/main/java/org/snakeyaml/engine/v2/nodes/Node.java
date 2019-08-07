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
package org.snakeyaml.engine.v2.nodes;


import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.exceptions.Mark;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for all nodes.
 * <p>
 * The nodes form the node-graph described in the <a
 * href="https://yaml.org/spec/1.2/spec.html">YAML Specification</a>.
 * </p>
 * <p>
 * While loading, the node graph is usually created by the
 * {@link org.snakeyaml.engine.v2.composer.Composer}.
 * </p>
 */
public abstract class Node {
    private Tag tag;
    private Optional<Mark> startMark;
    protected Optional<Mark> endMark;
    private boolean recursive;
    private Optional<Anchor> anchor;
    private Map<String, Object> properties;

    /**
     * true when the tag is assigned by the resolver
     */
    protected boolean resolved;

    /**
     * Create Node to be parsed
     *
     * @param tag       - the tag
     * @param startMark - start mark when available
     * @param endMark   - end mark when available
     */
    public Node(Tag tag, Optional<Mark> startMark, Optional<Mark> endMark) {
        setTag(tag);
        this.startMark = startMark;
        this.endMark = endMark;
        this.recursive = false;
        this.resolved = true;
        this.anchor = Optional.empty();
        this.properties = null;
    }

    /**
     * Tag of this node.
     * <p>
     * Every node has a tag assigned. The tag is either local or global.
     *
     * @return Tag of this node.
     */
    public Tag getTag() {
        return this.tag;
    }

    public Optional<Mark> getEndMark() {
        return endMark;
    }

    /**
     * @return scalar, sequence, mapping
     */
    public abstract NodeType getNodeType();

    public Optional<Mark> getStartMark() {
        return startMark;
    }

    public void setTag(Tag tag) {
        Objects.requireNonNull(tag, "tag in a Node is required.");
        this.tag = tag;
    }

    /**
     * Node is only equal to itself
     */
    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Indicates if this node must be constructed in two steps.
     * <p>
     * Two-step construction is required whenever a node is a child (direct or
     * indirect) of it self. That is, if a recursive structure is build using
     * anchors and aliases.
     * </p>
     * <p>
     * Set by {@link org.snakeyaml.engine.v2.composer.Composer}, used during the
     * construction process.
     * </p>
     * <p>
     * Only relevant during loading.
     * </p>
     *
     * @return <code>true</code> if the node is self referenced.
     */
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Get the anchor if it was defined for this Node
     *
     * @return the Anchor if available
     * @see <a href="https://yaml.org/spec/1.2/spec.html#id2765878">3.2.2.2. Anchors and Aliases</a>
     */
    public Optional<Anchor> getAnchor() {
        return anchor;
    }

    /**
     * Set the anchor for this Node
     *
     * @param anchor - the Anchor for this Node
     * @see <a href="https://yaml.org/spec/1.2/spec.html#id2765878">3.2.2.2. Anchors and Aliases</a>
     */
    public void setAnchor(Optional<Anchor> anchor) {
        this.anchor = anchor;
    }

    /**
     * Define a custom runtime property. It is not used by Engine but may be used by other tools.
     *
     * @param key   - the key for the custom property
     * @param value - the value for the custom property
     * @return the previous value for the provided key if it was defined
     */
    public Object setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties.put(key, value);
    }

    /**
     * Get the custom runtime property.
     *
     * @param key - the key of the runtime property
     * @return the value if it was specified
     */
    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        } else {
            return properties.get(key);
        }
    }
}
