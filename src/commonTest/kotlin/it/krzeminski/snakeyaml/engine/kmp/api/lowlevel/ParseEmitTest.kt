package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.StringOutputStream
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import okio.Buffer

/**
 * Test from https://github.com/yaml/yaml-runtimes
 */
class ParseEmitTest : FunSpec({
    test("parse and emit list") {
        val output = StringOutputStream()
        val input = "- 1\n- 2\n- 3"
        yamlToYaml(input, output)
        output.toString() shouldBe "- 1\n- 2\n- 3\n"
    }

    test("parse and emit map") {
        val output = StringOutputStream()
        val input = "---\nfoo: bar\n"
        yamlToYaml(input, output)
        output.toString() shouldBe "---\nfoo: bar\n"
    }
})

/**
 * Convert a YAML character stream into events and then emit events back to YAML.
 */
private fun yamlToYaml(input: String, output: StringOutputStream) {
    val parser = Parse(LoadSettings.builder().build())
    val emitter = Emitter(DumpSettings.builder().build(), output)
    for (event in parser.parse(input)) {
        emitter.emit(event)
    }
}
