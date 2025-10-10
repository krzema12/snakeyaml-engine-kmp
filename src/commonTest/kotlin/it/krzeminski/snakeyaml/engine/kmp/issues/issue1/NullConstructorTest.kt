package it.krzeminski.snakeyaml.engine.kmp.issues.issue1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class NullConstructorTest : FunSpec({
    test("custom constructor must be called without node") {
        val tagConstructors = mapOf(Tag.NULL to MyConstructNull())
        val settings = LoadSettings(tagConstructors = tagConstructors)
        val loader = Load(settings)
        val result = loader.loadOne("")
        result shouldNotBe null
        result shouldBe "absent"
    }

    test("custom constructor must be called with node") {
        val tagConstructors = mapOf(Tag.NULL to MyConstructNull())
        val settings = LoadSettings(tagConstructors = tagConstructors)
        val loader = Load(settings)
        val result = loader.loadOne("!!null null")
        result shouldBe "present"
    }
})

private class MyConstructNull : ConstructNode {
    override fun construct(node: Node?): Any {
        return if (node == null) {
            "absent"
        } else {
            "present"
        }
    }
}
