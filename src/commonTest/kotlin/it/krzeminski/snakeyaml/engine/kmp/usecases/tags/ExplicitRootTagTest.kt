package it.krzeminski.snakeyaml.engine.kmp.usecases.tags

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

/**
 * Example of serializing a root tag
 */
class ExplicitRootTagTest : FunSpec({
    test("explicit root tag") {
        val settings = DumpSettings.builder().setExplicitRootTag(Tag("!my-data")).build()
        val map = mapOf("foo" to "bar")
        val dump = Dump(settings)
        val output = dump.dumpToString(map)
        output shouldBe "!my-data {foo: bar}\n"
    }
})
