package it.krzeminski.snakeyaml.engine.kmp.nodes

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class MappingNodeTest : FunSpec({
    test("testToString") {
        val tuple1 = NodeTuple(
            ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN),
            ScalarNode(Tag.INT, "1", ScalarStyle.PLAIN)
        )
        val list = mutableListOf<NodeTuple>()
        list.add(tuple1)
        val mapping = MappingNode(Tag.MAP, list, FlowStyle.FLOW)
        val tuple2 = NodeTuple(ScalarNode(Tag.STR, "self", ScalarStyle.PLAIN), mapping)
        list.add(tuple2)
        val representation = mapping.toString()
        representation.substringBeforeLast(" value=") shouldBe "<MappingNode (tag=tag:yaml.org,2002:map, values={ key=<ScalarNode (tag=tag:yaml.org,2002:str, value=a)>; value=<NodeTuple keyNode=<ScalarNode (tag=tag:yaml.org,2002:str, value=a)>; valueNode=<ScalarNode (tag=tag:yaml.org,2002:int, value=1)>> }{ key=<ScalarNode (tag=tag:yaml.org,2002:str, value=self)>;"
    }
})
