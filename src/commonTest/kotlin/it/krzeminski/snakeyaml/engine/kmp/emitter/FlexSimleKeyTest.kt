package it.krzeminski.snakeyaml.engine.kmp.emitter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class FlexSimleKeyTest : FunSpec({
    val len = 130

    test("testLongKey") {
        val dump = Dump(createOptions(len))
        val root = mutableMapOf<String, Any>()
        val map = mutableMapOf<String, String>()
        val key = createKey(len)
        map[key] = "v1"
        root["data"] = map
        dump.dumpToString(root) shouldBe "data: {? $key\n  : v1}\n"
    }

    test("testForceLongKeyToBeImplicit") {
        val dump = Dump(createOptions(len + 10))
        val root = mutableMapOf<String, Any>()
        val map = mutableMapOf<String, String>()
        val key = createKey(len)
        map[key] = "v1"
        root["data"] = map
        dump.dumpToString(root) shouldBe "data: {$key: v1}\n"
    }

    test("testTooLongKeyLength") {
        val exception = shouldThrow<YamlEngineException> {
            createOptions(1024 + 1)
        }
        exception.message shouldBe "The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.2/spec.html#id2798057"
    }
})

private fun createOptions(len: Int): DumpSettings {
    return DumpSettings.builder().setMaxSimpleKeyLength(len).build()
}

private fun createKey(length: Int): String {
    val outputBuffer = StringBuilder(length)
    for (i in 0 until length) {
        outputBuffer.append("" + (i + 1) % 10)
    }
    val prefix = length.toString()
    val result = prefix + "_" + outputBuffer.toString().substring(0, length - prefix.length - 1)
    if (result.length != length) {
        throw RuntimeException("It was: " + result.length)
    }
    return result
}
