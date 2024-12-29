package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import CommonTestResources
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import okio.ByteString

class DereferenceAliasesTest : FunSpec({
    test("no aliases") {
        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val str = stringFromResources("issues/issue1086-1-input.yaml")
        println(str)
        val map = load.loadOne(stringFromResources("issues/issue1086-1-input.yaml")) as Map<*, *>?
        val setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true).build()
        val dump = Dump(setting)
        val node = dump.dumpToString(map)
        val expected = issue1086_1_expected_yaml
        expected shouldBe node
    }

    test("no aliases recursive") {
        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val map = load.loadOne(issue1086_2_input_yaml) as Map<*, *>?
        val setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true).build()
        val dump = Dump(setting)
        shouldThrow<YamlEngineException> {
            dump.dumpToString(map)
        }.also {
            it.message shouldBe "Cannot dereference aliases for recursive structures."
        }
    }
})

private fun stringFromResources(path: String): String {
    val segments = path.split("/")
    return (segments
        .dropLast(1)
        .fold(CommonTestResources().resourcesMap) { map, segment ->
            @Suppress("UNCHECKED_CAST")
            map[segment] as Map<String, Any>
    }[segments.last()] as ByteString)
}
