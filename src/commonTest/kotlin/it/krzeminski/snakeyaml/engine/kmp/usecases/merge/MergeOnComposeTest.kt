package it.krzeminski.snakeyaml.engine.kmp.usecases.merge

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Present
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class MergeOnComposeTest : FunSpec({
    test("simple load merge") {
        val out = merge(
            inputName = "/merge/issue1096-simple-merge-input.yaml",
            loadSettings = LoadSettings.builder().setSchema(CoreSchema()).build(),
        )
        val expected = stringFromResources("/merge/issue1096-simple-merge-output.yaml")
        out shouldBe expected
    }

    test("complex load merge") {
        val out = merge(
            inputName = "/merge/issue1096-complex-merge-input.yaml",
            loadSettings = LoadSettings.builder().setSchema(CoreSchema()).build(),
        )
        val expected = stringFromResources("/merge/issue1096-complex-merge-output.yaml")
        out shouldBe expected
    }

    test("specs load merge") {
        val out = merge(
            inputName = "/merge/issue1096-merge-input.yaml",
            loadSettings = LoadSettings.builder().setSchema(CoreSchema()).build(),
        )
        val expected = stringFromResources("/merge/issue1096-merge-output.yaml")
        out shouldBe expected
    }

    test("merge as scalar") {
        val str = """
            test-list:
             - &1
              a: 1
              b: 2
             - &2 <<: *1
             - <<: *2""".trimIndent()

        val loader = Compose(LoadSettings.builder()
            .setSchema(CoreSchema())
            .setParseComments(false)
            .build())

        shouldThrow<Exception> {
            loader.compose(str)
        }.also {
            it.message shouldContain "Expected mapping node or an anchor referencing mapping"
            it.message shouldContain "in reader, line 6, column 10:"
        }
    }
})

private fun merge(inputName: String, loadSettings: LoadSettings): String {
    val input = stringFromResources(inputName)
    val loader = Compose(loadSettings)
    val sourceTree = loader.compose(input)!!
    val serialize = Serialize(DumpSettings.builder()
        .setDereferenceAliases(true)
        .build())
    val events = serialize.serializeOne(sourceTree)
    val present = Present(DumpSettings.builder().build())

    return present.emitToString(events.iterator())
}
