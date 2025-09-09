package it.krzeminski.snakeyaml.engine.kmp.nodes

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class MappingNodeTest : FunSpec({
    test("toString") {
        val list = mutableListOf<NodeTuple>()
        val tuple1 = NodeTuple(
            ScalarNode(tag = Tag.STR, value = "a", scalarStyle = ScalarStyle.PLAIN),
            ScalarNode(tag = Tag.INT, value = "1", scalarStyle = ScalarStyle.PLAIN)
        )
        list.add(tuple1)
        val mapping = MappingNode(tag = Tag.MAP, value = list, flowStyle = FlowStyle.FLOW)
        val tuple2 = NodeTuple(ScalarNode(tag = Tag.STR, value = "self", scalarStyle = ScalarStyle.PLAIN), mapping)
        list.add(tuple2)
        val representation = mapping.toString()
        representation.substringBeforeLast(" value=") shouldBe "<MappingNode (" +
            "tag=tag:yaml.org,2002:map, values={ " +
            "key=<ScalarNode (" +
            "tag=tag:yaml.org,2002:str, value=a" +
            ")>; " +
            "value=<NodeTuple keyNode=<ScalarNode (" +
            "tag=tag:yaml.org,2002:str, value=a" +
            ")>; " +
            "valueNode=<ScalarNode (" +
            "tag=tag:yaml.org,2002:int, value=1" +
            ")>> " +
            "}" +
            "{ key=<ScalarNode (" +
            "tag=tag:yaml.org,2002:str, value=self" +
            ")>;"
    }
})
