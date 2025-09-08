package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.common.Platform
import io.kotest.common.platform
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class ScalarResolverTupleTest : FunSpec({
    test("toString()") {
        ResolverTuple(Tag.STR, Regex("^(?:true|false)$")).toString() shouldBe if (platform == Platform.JVM) {
            "Tuple tag=tag:yaml.org,2002:str regexp=^(?:true|false)$"
        } else {
            "Tuple tag=tag:yaml.org,2002:str regexp=/^(?:true|false)$/gu"
        }
    }
})
