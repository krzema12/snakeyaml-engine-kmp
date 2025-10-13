package it.krzeminski.snakeyaml.engine.kmp.emitter

import io.kotest.assertions.AssertionErrorBuilder.Companion.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrowWithMessage
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class FlexSimleKeyTest : FunSpec({
    val len = 130

    test("long key") {
        val dump = Dump(createOptions(len))
        val key = createKey(len)
        val map = mapOf(key to "v1")
        val root = mapOf("data" to map)
        dump.dumpToString(root) shouldBe "data: {? $key\n  : v1}\n"
    }

    test("force long key to be implicit") {
        val dump = Dump(createOptions(len + 10))
        val key = createKey(len)
        val map = mapOf(key to "v1")
        val root = mapOf("data" to map)
        dump.dumpToString(root) shouldBe "data: {$key: v1}\n"
    }

    test("test too long key length") {
        shouldThrowWithMessage<YamlEngineException>(message = "The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.2/spec.html#id2798057") {
            createOptions(1024 + 1)
        }
    }
})

private fun createOptions(len: Int): DumpSettings {
    return DumpSettings(maxSimpleKeyLength = len)
}

private fun createKey(length: Int): String {
    val outputBuffer = StringBuilder(length)
    for (i in 0 until length) {
        outputBuffer.append("" + (i + 1) % 10)
    }
    val prefix = length.toString()
    val result = "${prefix}_${outputBuffer.toString().substring(0, length - prefix.length - 1)}"
    if (result.length != length) {
        fail("It was: ${result.length}")
    }
    return result
}
