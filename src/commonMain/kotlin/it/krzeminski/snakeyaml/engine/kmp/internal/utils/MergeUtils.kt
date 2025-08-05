package it.krzeminski.snakeyaml.engine.kmp.internal.utils

import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.NodeTuple
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag.Companion.MERGE

abstract class MergeUtils {
    /**
      * Converts the specified [node] into a [MappingNode].
      *
      * This method is designed to transform various types of [Node] into a [MappingNode],
      * enabling further processing such as merging of keys.
      *
      * @param node The node to be transformed.
      * @return A [MappingNode] representation of the input [node].
      */
    abstract fun asMappingNode(node: Node): MappingNode

    /**
     * Processes and resolves merge keys in a [MappingNode], merging resolved key/values into the node.
     *
     * Implements the YAML merge key feature by examining the nodes within the provided [node]
     * and merging keys from referenced by "merge key" map(s) into the current mapping as per the YAML
     * specification. Handling of duplicate keys is defined by the order of appearance in the mapping
     * node, with priority given to the keys defined in [node] and the earliest occurrences
     * in the merging ones.
     *
     * See also: [YAML Merge Key Specification](https://yaml.org/type/merge.html)
     *
     * @param node The MappingNode to process for merge keys.
     * @return A list of [NodeTuple] containing the merged keys and values.
     */
    fun flatten(node: MappingNode): List<NodeTuple> {
        var toProcess = node.value
        var result = toProcess
        var process = true

        while (process) {
            process = false
            val updated = ArrayList<NodeTuple>(toProcess.size)
            val keys = HashSet<String>(toProcess.size)
            val merges = ArrayList<NodeTuple>(2)

            for (tuple in toProcess) {
                val keyNode = tuple.keyNode
                if (keyNode.tag == MERGE) {
                    merges += tuple
                } else {
                    updated += tuple
                    if (keyNode is ScalarNode) {
                        keys += keyNode.value
                    }
                }
            }

            for (tuple in merges) {
                val valueNode = tuple.valueNode
                if (valueNode is SequenceNode) {
                    for (ref in valueNode.value) {
                        val mergable = asMappingNode(ref)
                        process = process || mergable.hasMergeTag
                        val filtered = filter(mergable.value, keys)
                        updated += filtered.first
                        keys += filtered.second
                    }
                } else {
                    val mergable = asMappingNode(valueNode)
                    process = process || mergable.hasMergeTag
                    val filtered = filter(mergable.value, keys)
                    updated += filtered.first
                    keys += filtered.second
                }
            }
            result = updated
            if (process) {
                toProcess = updated
            }
        }
        return result
    }

    private fun filter(mergables: List<NodeTuple>, filter: Set<String>): Pair<List<NodeTuple>, Set<String>> {
        val size = mergables.size
        val keys = HashSet<String>(size)
        val result = ArrayList<NodeTuple>(size)

        for (tuple in mergables) {
            val key = tuple.keyNode
            if (key is ScalarNode) {
                val nodeValue = key.value
                if (nodeValue !in filter) {
                    result += tuple
                    keys += nodeValue
                }
            } else {
                result += tuple
            }
        }

        return Pair(result, keys)
    }
}
