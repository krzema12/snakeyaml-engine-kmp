package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.StringOutputStream

class DumpTest : FunSpec({
    test("Dump string") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString("a")
        str shouldBe "a\n"
    }

    test("Dump int") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(1)
        str shouldBe "1\n"
    }

    test("Dump boolean") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(true)
        str shouldBe "true\n"
    }

    test("Dump seq") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(listOf(2, "a", true))
        str shouldBe "[2, a, true]\n"
    }

    test("Dump map") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val output = dump.dumpToString(mapOf("x" to 1, "y" to 2, "z" to 3))
        output shouldBe "{x: 1, y: 2, z: 3}\n"
    }

    test("Dump all instances") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val streamToStringWriter = StringStreamDataWriter()
        val list = mutableListOf<Any?>("a", null, true)
        dump.dumpAll(list.iterator(), streamToStringWriter)
        streamToStringWriter.toString() shouldBe "a\n--- null\n--- true\n"
        // load back
        val load = Load()
        for (obj in load.loadAll(streamToStringWriter.toString())) {
            obj shouldBe list.removeAt(0)
        }
    }

    test("Dump all instances to String") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val list = mutableListOf<Any?>("a", null, true)
        val output = dump.dumpAllToString(list.iterator())
        output shouldBe "a\n--- null\n--- true\n"
        // load back
        val load = Load()
        for (obj in load.loadAll(output)) {
            obj shouldBe list.removeAt(0)
        }
    }

    test("Dump to stream") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val outputStream = StringOutputStream()
        val writer = YamlOutputStreamWriter(outputStream)
        dump.dump(mapOf("x" to 1, "y" to 2, "z" to 3), writer)
        outputStream.toString() shouldBe "{x: 1, y: 2, z: 3}\n"
    }
})
