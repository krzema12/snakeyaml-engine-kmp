package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class ScalarResolverTupleTest : FunSpec({

    test("ResolverTuple.toString()") {
        ResolverTuple(Tag.STR, Regex("^(?:true|false)$")).toString() shouldBe "Tuple tag=tag:yaml.org,2002:str regexp=^(?:true|false)$"
    }
})
