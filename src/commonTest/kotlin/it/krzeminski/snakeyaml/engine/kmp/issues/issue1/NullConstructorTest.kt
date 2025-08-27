package it.krzeminski.snakeyaml.engine.kmp.issues.issue1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

private class MyConstructNull : ConstructNode {
    override fun construct(node: Node?): Any {
        return if (node == null) {
            "absent"
        } else {
            "present"
        }
    }
}

class NullConstructorTest : FunSpec({
    test("Custom constructor must be called without node") {
        val tagConstructors = mutableMapOf<Tag, ConstructNode>()
        tagConstructors[Tag.NULL] = MyConstructNull()
        val settings = LoadSettings.builder().setTagConstructors(tagConstructors).build()
        val loader = Load(settings)
        val result = loader.loadOne("")
        result shouldNotBe null
        result shouldBe "absent"
    }

    test("Custom constructor must be called with node") {
        val tagConstructors = mutableMapOf<Tag, ConstructNode>()
        tagConstructors[Tag.NULL] = MyConstructNull()
        val settings = LoadSettings.builder().setTagConstructors(tagConstructors).build()
        val loader = Load(settings)
        val result = loader.loadOne("!!null null")
        result shouldBe "present"
    }
})
