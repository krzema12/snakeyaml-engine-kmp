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
package org.snakeyaml.engine.v1.constructor;

import org.snakeyaml.engine.v1.api.ConstructNode;
import org.snakeyaml.engine.v1.api.LoadSettings;
import org.snakeyaml.engine.v1.exceptions.ConstructorException;
import org.snakeyaml.engine.v1.exceptions.DuplicateKeyException;
import org.snakeyaml.engine.v1.exceptions.YamlEngineException;
import org.snakeyaml.engine.v1.nodes.*;

import java.math.BigInteger;
import java.util.*;

/**
 * Construct standard Java classes
 */
public class StandardConstructor extends BaseConstructor {

    public StandardConstructor(LoadSettings settings) {
        super(settings);
        this.tagConstructors.put(Tag.NULL, new ConstructYamlNull());
        this.tagConstructors.put(Tag.BOOL, new ConstructYamlBool());
        this.tagConstructors.put(Tag.INT, new ConstructYamlInt());
        this.tagConstructors.put(Tag.FLOAT, new ConstructYamlFloat());
        this.tagConstructors.put(Tag.BINARY, new ConstructYamlBinary());
        this.tagConstructors.put(Tag.SET, new ConstructYamlSet());
        this.tagConstructors.put(Tag.STR, new ConstructYamlStr());
        this.tagConstructors.put(Tag.SEQ, new ConstructYamlSeq());
        this.tagConstructors.put(Tag.MAP, new ConstructYamlMap());

        this.tagConstructors.put(new Tag(UUID.class), new ConstructUuidClass());
        this.tagConstructors.put(new Tag(Optional.class), new ConstructOptionalClass());

        this.tagConstructors.putAll(settings.getTagConstructors());
    }

    protected void flattenMapping(MappingNode node) {
        // perform merging only on nodes containing merge node(s)
        processDuplicateKeys(node);
        if (node.isMerged()) {
            node.setValue(mergeNode(node, true, new HashMap<Object, Integer>(),
                    new ArrayList<NodeTuple>()));
        }
    }

    protected void processDuplicateKeys(MappingNode node) {
        List<NodeTuple> nodeValue = node.getValue();
        Map<Object, Integer> keys = new HashMap<Object, Integer>(nodeValue.size());
        TreeSet<Integer> toRemove = new TreeSet<Integer>();
        int i = 0;
        for (NodeTuple tuple : nodeValue) {
            Node keyNode = tuple.getKeyNode();
            if (!keyNode.getTag().equals(Tag.MERGE)) {
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

                Integer prevIndex = keys.put(key, i);
                if (prevIndex != null) {
                    if (!settings.getAllowDuplicateKeys()) {
                        throw new DuplicateKeyException(node.getStartMark(), key,
                                tuple.getKeyNode().getStartMark());
                    }
                    toRemove.add(prevIndex);
                }
            }
            i = i + 1;
        }

        Iterator<Integer> indicies2remove = toRemove.descendingIterator();
        while (indicies2remove.hasNext()) {
            nodeValue.remove(indicies2remove.next().intValue());
        }
    }

    /**
     * Does merge for supplied mapping node.
     *
     * @param node        where to merge
     * @param isPreffered true if keys of node should take precedence over others...
     * @param key2index   maps already merged keys to index from values
     * @param values      collects merged NodeTuple
     * @return list of the merged NodeTuple (to be set as value for the
     * MappingNode)
     */
    private List<NodeTuple> mergeNode(MappingNode node, boolean isPreffered,
                                      Map<Object, Integer> key2index, List<NodeTuple> values) {
        Iterator<NodeTuple> iter = node.getValue().iterator();
        while (iter.hasNext()) {
            final NodeTuple nodeTuple = iter.next();
            final Node keyNode = nodeTuple.getKeyNode();
            final Node valueNode = nodeTuple.getValueNode();
            if (keyNode.getTag().equals(Tag.MERGE)) {
                iter.remove();
                switch (valueNode.getNodeType()) {
                    case MAPPING:
                        MappingNode mn = (MappingNode) valueNode;
                        mergeNode(mn, false, key2index, values);
                        break;
                    case SEQUENCE:
                        SequenceNode sn = (SequenceNode) valueNode;
                        List<Node> vals = sn.getValue();
                        for (Node subnode : vals) {
                            if (!(subnode instanceof MappingNode)) {
                                throw new ConstructorException("while constructing a mapping",
                                        node.getStartMark(),
                                        "expected a mapping for merging, but found "
                                                + subnode.getNodeType(),
                                        subnode.getStartMark());
                            }
                            MappingNode mnode = (MappingNode) subnode;
                            mergeNode(mnode, false, key2index, values);
                        }
                        break;
                    default:
                        throw new ConstructorException("while constructing a mapping",
                                node.getStartMark(),
                                "expected a mapping or list of mappings for merging, but found "
                                        + valueNode.getNodeType(),
                                valueNode.getStartMark());
                }
            } else {
                // we need to construct keys to avoid duplications
                Object key = constructObject(keyNode);
                if (!key2index.containsKey(key)) { // 1st time merging key
                    values.add(nodeTuple);
                    // keep track where tuple for the key is
                    key2index.put(key, values.size() - 1);
                } else if (isPreffered) { // there is value for the key, but we
                    // need to override it
                    // change value for the key using saved position
                    values.set(key2index.get(key), nodeTuple);
                }
            }
        }
        return values;
    }

    @Override
    protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
        flattenMapping(node);
        super.constructMapping2ndStep(node, mapping);
    }

    @Override
    protected void constructSet2ndStep(MappingNode node, Set<Object> set) {
        flattenMapping(node);
        super.constructSet2ndStep(node, set);
    }

    public class ConstructYamlNull implements ConstructNode {
        @Override
        public Object construct(Node node) {
            if (node != null) constructScalar((ScalarNode) node);
            return null;
        }
    }

    private final static Map<String, Boolean> BOOL_VALUES = new HashMap();

    static {
        BOOL_VALUES.put("true", Boolean.TRUE);
        BOOL_VALUES.put("false", Boolean.FALSE);
    }

    public class ConstructYamlBool implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String val = constructScalar((ScalarNode) node);
            return BOOL_VALUES.get(val.toLowerCase());
        }
    }

    public class ConstructYamlInt implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String value = constructScalar((ScalarNode) node);
            return createIntNumber(value);
        }
    }

    private Number createIntNumber(String number) {
        Number result;
        try {
            //first try integer
            result = Integer.valueOf(number);
        } catch (NumberFormatException e) {
            try {
                //then Long
                result = Long.valueOf(number);
            } catch (NumberFormatException e1) {
                //and BigInteger as the last resource
                result = new BigInteger(number);
            }
        }
        return result;
    }

    public class ConstructYamlFloat implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String value = constructScalar((ScalarNode) node);
            return Double.valueOf(value);
        }
    }

    public class ConstructYamlBinary implements ConstructNode {
        @Override
        public Object construct(Node node) {
            // Ignore white spaces for base64 encoded scalar
            String noWhiteSpaces = constructScalar((ScalarNode) node).replaceAll("\\s", "");
            return Base64.getDecoder().decode(noWhiteSpaces);
        }
    }

    public class ConstructUuidClass implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String uuidValue = constructScalar((ScalarNode) node);
            return UUID.fromString(uuidValue);
        }
    }

    public class ConstructOptionalClass implements ConstructNode {
        @Override
        public Object construct(Node node) {
            if (node.getNodeType() != NodeType.SCALAR) {
                throw new ConstructorException("while constructing Optional", Optional.empty(), "found non scalar node", node.getStartMark());
            }
            String value = constructScalar((ScalarNode) node);
            Tag implicitTag = settings.getScalarResolver().resolve(value, true);
            if (implicitTag.equals(Tag.NULL)) {
                return Optional.empty();
            } else {
                return Optional.of(value);
            }
        }
    }

    public class ConstructYamlOmap implements ConstructNode {
        @Override
        public Object construct(Node node) {
            // Note: we do not check for duplicate keys, because it's too
            // CPU-expensive.
            Map<Object, Object> omap = new LinkedHashMap<Object, Object>();
            if (!(node instanceof SequenceNode)) {
                throw new ConstructorException("while constructing an ordered map",
                        node.getStartMark(), "expected a sequence, but found " + node.getNodeType(),
                        node.getStartMark());
            }
            SequenceNode snode = (SequenceNode) node;
            for (Node subnode : snode.getValue()) {
                if (!(subnode instanceof MappingNode)) {
                    throw new ConstructorException("while constructing an ordered map",
                            node.getStartMark(),
                            "expected a mapping of length 1, but found " + subnode.getNodeType(),
                            subnode.getStartMark());
                }
                MappingNode mnode = (MappingNode) subnode;
                if (mnode.getValue().size() != 1) {
                    throw new ConstructorException("while constructing an ordered map",
                            node.getStartMark(), "expected a single mapping item, but found "
                            + mnode.getValue().size() + " items",
                            mnode.getStartMark());
                }
                Node keyNode = mnode.getValue().get(0).getKeyNode();
                Node valueNode = mnode.getValue().get(0).getValueNode();
                Object key = constructObject(keyNode);
                Object value = constructObject(valueNode);
                omap.put(key, value);
            }
            return omap;
        }
    }


    public class ConstructYamlSet implements ConstructNode {
        @Override
        public Object construct(Node node) {
            if (node.isRecursive()) {
                return (constructedObjects.containsKey(node) ? constructedObjects.get(node)
                        : createDefaultSet(((MappingNode) node).getValue().size()));
            } else {
                return constructSet((MappingNode) node);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void constructRecursive(Node node, Object object) {
            if (node.isRecursive()) {
                constructSet2ndStep((MappingNode) node, (Set<Object>) object);
            } else {
                throw new YamlEngineException("Unexpected recursive set structure. Node: " + node);
            }
        }
    }

    public class ConstructYamlStr implements ConstructNode {
        @Override
        public Object construct(Node node) {
            return constructScalar((ScalarNode) node);
        }
    }

    public class ConstructYamlSeq implements ConstructNode {
        @Override
        public Object construct(Node node) {
            SequenceNode seqNode = (SequenceNode) node;
            if (node.isRecursive()) {
                return settings.getDefaultList().apply(seqNode.getSequence().size());
            } else {
                return constructSequence(seqNode);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void constructRecursive(Node node, Object data) {
            if (node.isRecursive()) {
                constructSequenceStep2((SequenceNode) node, (List<Object>) data);
            } else {
                throw new YamlEngineException("Unexpected recursive sequence structure. Node: " + node);
            }
        }
    }

    public class ConstructYamlMap implements ConstructNode {
        @Override
        public Object construct(Node node) {
            MappingNode mnode = (MappingNode) node;
            if (node.isRecursive()) {
                return createDefaultMap(mnode.getValue().size());
            } else {
                return constructMapping(mnode);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void constructRecursive(Node node, Object object) {
            if (node.isRecursive()) {
                constructMapping2ndStep((MappingNode) node, (Map<Object, Object>) object);
            } else {
                throw new YamlEngineException("Unexpected recursive mapping structure. Node: " + node);
            }
        }
    }
}
