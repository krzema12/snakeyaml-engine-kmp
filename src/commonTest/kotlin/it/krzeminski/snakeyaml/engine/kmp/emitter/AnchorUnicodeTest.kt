package it.krzeminski.snakeyaml.engine.kmp.emitter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.serializer.AnchorGenerator

class AnchorUnicodeTest : FunSpec({
    val invalidAnchor = setOf('[', ']', '{', '}', ',', '*', '&')

    test("unicode anchor") {
        val settings = DumpSettings.builder().setAnchorGenerator(object : AnchorGenerator {
            private var id = 0
            override fun nextAnchor(node: Node): Anchor {
                return Anchor("タスク${id++}")
            }
        }).build()
        val dump = Dump(settings)
        val list = listOf("abc")

        val toExport = listOf(list, list)

        val output = dump.dumpToString(toExport)
        output shouldBe "- &タスク0 [abc]\n- *タスク0\n"
    }

    test("invalid anchor") {
        for (ch in invalidAnchor) {
            val dump = Dump(createSettings(ch))
            val list = listOf("abc")

            val toExport = listOf(list, list)

            shouldThrow<Exception> {
                dump.dumpToString(toExport)
            }.also {
                it.message shouldBe "Invalid character '$ch' in the anchor: anchor$ch"
            }
        }
    }

    test("anchors") {
        Anchor("a").toString() shouldBe "a"
        checkAnchor("a ") shouldBe "Anchor may not contain spaces: a "
        checkAnchor("a \t") shouldBe "Anchor may not contain spaces: a \t"
        checkAnchor("a[") shouldBe "Invalid character '[' in the anchor: a["
        checkAnchor("a]") shouldBe "Invalid character ']' in the anchor: a]"
        checkAnchor("{a") shouldBe "Invalid character '{' in the anchor: {a"
        checkAnchor("}a") shouldBe "Invalid character '}' in the anchor: }a"
        checkAnchor("a,b") shouldBe "Invalid character ',' in the anchor: a,b"
        checkAnchor("a*b") shouldBe "Invalid character '*' in the anchor: a*b"
        checkAnchor("a&b") shouldBe "Invalid character '&' in the anchor: a&b"
    }
})

private fun createSettings(invalid: Char): DumpSettings {
    return DumpSettings.builder().setAnchorGenerator { Anchor("anchor$invalid") }.build()
}

private fun checkAnchor(a: String): String? {
    return shouldThrow<Exception> {
        Anchor(a).toString()
    }.message
}
