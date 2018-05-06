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
package org.snakeyaml.engine.constructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.snakeyaml.engine.api.ConstructNode;
import org.snakeyaml.engine.api.LoadSettings;
import org.snakeyaml.engine.exceptions.ConstructorException;
import org.snakeyaml.engine.nodes.MappingNode;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.nodes.NodeTuple;
import org.snakeyaml.engine.nodes.ScalarNode;
import org.snakeyaml.engine.nodes.SequenceNode;
import org.snakeyaml.engine.nodes.Tag;

public abstract class BaseConstructor {

    protected LoadSettings settings;
    /**
     * It maps the (explicit or implicit) tag to the Construct implementation.
     */
    protected final Map<Tag, ConstructNode> tagConstructors;
    final Map<Node, Object> constructedObjects;
    private final Set<Node> recursiveObjects;
    private final ArrayList<RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>>> maps2fill;
    private final ArrayList<RecursiveTuple<Set<Object>, Object>> sets2fill;

    public BaseConstructor(LoadSettings settings) {
        this.settings = settings;
        tagConstructors = settings.getTagConstructors();
        constructedObjects = new HashMap();
        recursiveObjects = new HashSet();
        maps2fill = new ArrayList();
        sets2fill = new ArrayList();
    }

    /**
     * Ensure that the stream contains a single document and construct it
     * @param optionalNode - composed Node
     * @return constructed instance
     */
    public Object constructSingleDocument(Optional<Node> optionalNode) {
        if (!optionalNode.isPresent() || Tag.NULL.equals(optionalNode.get().getTag())) {
            return null;
        } else {
            settings.getRootConstructNode().ifPresent(constructNode -> optionalNode.get().setConstruct(constructNode));
            return construct(optionalNode.get());
        }
    }

    /**
     * Construct complete YAML document. Call the second step in case of
     * recursive structures. At the end cleans all the state.
     *
     * @param node root Node
     * @return Java instance
     */
    protected Object construct(Node node) {
        Object data = constructObject(node);
        fillRecursive();
        constructedObjects.clear();
        recursiveObjects.clear();
        return data;
    }

    private void fillRecursive() {
        if (!maps2fill.isEmpty()) {
            for (RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>> entry : maps2fill) {
                RecursiveTuple<Object, Object> key_value = entry._2();
                entry._1().put(key_value._1(), key_value._2());
            }
            maps2fill.clear();
        }
        if (!sets2fill.isEmpty()) {
            for (RecursiveTuple<Set<Object>, Object> value : sets2fill) {
                value._1().add(value._2());
            }
            sets2fill.clear();
        }
    }

    /**
     * Construct object from the specified Node. Return existing instance if the
     * node is already constructed.
     *
     * @param node Node to be constructed
     * @return Java instance
     */
    protected Object constructObject(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (constructedObjects.containsKey(node)) {
            return constructedObjects.get(node);
        }
        return constructObjectNoCheck(node);
    }

    protected Object constructObjectNoCheck(Node node) {
        if (recursiveObjects.contains(node)) {
            throw new ConstructorException(null, Optional.empty(), "found unconstructable recursive node",
                    node.getStartMark());
        }
        recursiveObjects.add(node);
        ConstructNode constructor = getConstructor(node);
        Object data = (constructedObjects.containsKey(node)) ? constructedObjects.get(node)
                : constructor.construct(node);

        constructedObjects.put(node, data);
        recursiveObjects.remove(node);
        if (node.isRecursive()) {
            constructor.constructRecursive(node, data);
        }
        return data;
    }

    /**
     * Select {@link ConstructNode} inside the provided {@link Node} or the one associated with the {@link Tag}
     *
     * @param node {@link Node} to construct an instance from
     * @return {@link ConstructNode} implementation for the specified node
     */
    protected ConstructNode getConstructor(Node node) {
        if (node.getConstruct().isPresent()) {
            return node.getConstruct().get();
        } else {
            return tagConstructors.getOrDefault(node.getTag(), getDefaultConstruct(node));
        }
    }

    abstract ConstructNode getDefaultConstruct(Node node);

    protected String constructScalar(ScalarNode node) {
        return node.getValue();
    }

    // >>>> DEFAULTS >>>>
    protected List<Object> createDefaultList(int initSize) {
        return new ArrayList<Object>(initSize);
    }

    protected Set<Object> createDefaultSet(int initSize) {
        return new LinkedHashSet<Object>(initSize);
    }

    protected Map<Object, Object> createDefaultMap(int initSize) {
        // respect order from YAML document
        return new LinkedHashMap<Object, Object>(initSize);
    }

    protected Object createArray(Class<?> type, int size) {
        return Array.newInstance(type.getComponentType(), size);
    }

    // <<<< DEFAULTS <<<<

    //TODOprotected Object finalizeConstruction(Node node, Object data) {


    // <<<< NEW instance

    // >>>> Construct => NEW, 2ndStep(filling)
    protected List<? extends Object> constructSequence(SequenceNode node) {
        List<Object> result = settings.getDefaultList().apply(node.getSequence().size());
        constructSequenceStep2(node, result);
        return result;
    }


    protected void constructSequenceStep2(SequenceNode node, Collection<Object> collection) {
        for (Node child : node.getValue()) {
            collection.add(constructObject(child));
        }
    }

    protected Set<Object> constructSet(MappingNode node) {
        final Set<Object> set = settings.getDefaultSet().apply(node.getMapping().size());
        constructSet2ndStep(node, set);
        return set;
    }

    protected Map<Object, Object> constructMapping(MappingNode node) {
        final Map<Object, Object> mapping = settings.getDefaultMap().apply(node.getMapping().size());
        constructMapping2ndStep(node, mapping);
        return mapping;
    }

    protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
        List<NodeTuple> nodeValue = node.getValue();
        for (NodeTuple tuple : nodeValue) {
            Node keyNode = tuple.getKeyNode();
            Node valueNode = tuple.getValueNode();
            Object key = constructObject(keyNode);
            if (key != null) {
                try {
                    key.hashCode();// check circular dependencies
                } catch (Exception e) {
                    throw new ConstructorException("while constructing a mapping",
                            node.getStartMark(), "found unacceptable key " + key,
                            tuple.getKeyNode().getStartMark(), e);
                }
            }
            Object value = constructObject(valueNode);
            if (keyNode.isRecursive()) {
                /*
                 * if keyObject is created it 2 steps we should postpone putting
                 * it in map because it may have different hash after
                 * initialization compared to clean just created one. And map of
                 * course does not observe key hashCode changes.
                 */
                maps2fill.add(0,
                        new RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>>(
                                mapping, new RecursiveTuple<Object, Object>(key, value)));
            } else {
                mapping.put(key, value);
            }
        }
    }

    protected void constructSet2ndStep(MappingNode node, Set<Object> set) {
        List<NodeTuple> nodeValue = node.getValue();
        for (NodeTuple tuple : nodeValue) {
            Node keyNode = tuple.getKeyNode();
            Object key = constructObject(keyNode);
            if (key != null) {
                try {
                    key.hashCode();// check circular dependencies
                } catch (Exception e) {
                    throw new ConstructorException("while constructing a Set", node.getStartMark(),
                            "found unacceptable key " + key, tuple.getKeyNode().getStartMark(), e);
                }
            }
            if (keyNode.isRecursive()) {
                /*
                 * if keyObject is created it 2 steps we should postpone putting
                 * it into the set because it may have different hash after
                 * initialization compared to clean just created one. And set of
                 * course does not observe value hashCode changes.
                 */
                sets2fill.add(0, new RecursiveTuple<Set<Object>, Object>(set, key));
            } else {
                set.add(key);
            }
        }
    }

    // <<<< Costruct => NEW, 2ndStep(filling)


    private static class RecursiveTuple<T, K> {
        private final T _1;
        private final K _2;

        public RecursiveTuple(T _1, K _2) {
            this._1 = _1;
            this._2 = _2;
        }

        public K _2() {
            return _2;
        }

        public T _1() {
            return _1;
        }
    }
}
