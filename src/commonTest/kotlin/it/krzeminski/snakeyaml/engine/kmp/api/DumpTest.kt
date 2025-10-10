package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.StringOutputStream

class DumpTest : FunSpec({
    test("dump string") {
        val settings = DumpSettings()
        val dump = Dump(settings)
        val str = dump.dumpToString("a")
        str shouldBe "a\n"
    }

    test("dump int") {
        val settings = DumpSettings()
        val dump = Dump(settings)
        val str = dump.dumpToString(1)
        str shouldBe "1\n"
    }

    test("dump boolean") {
        val settings = DumpSettings()
        val dump = Dump(settings)
        val str = dump.dumpToString(true)
        str shouldBe "true\n"
    }

    test("dump seq") {
        val settings = DumpSettings()
        val dump = Dump(settings)
        val str = dump.dumpToString(listOf(2, "a", true))
        str shouldBe "[2, a, true]\n"
    }

    test("dump map") {
        val settings = DumpSettings()
        val dump = Dump(settings)
        val output = dump.dumpToString(mapOf("x" to 1, "y" to 2, "z" to 3))
        output shouldBe "{x: 1, y: 2, z: 3}\n"
    }

    test("dump all instances") {
        val settings = DumpSettings()
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

    test("dump all instances to String") {
        val settings = DumpSettings()
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

    test("dump to stream") {
        val settings = DumpSettings()
        val dump = Dump(settings)
        val outputStream = StringOutputStream()
        dump.dump(mapOf("x" to 1, "y" to 2, "z" to 3), outputStream)
        outputStream.toString() shouldBe "{x: 1, y: 2, z: 3}\n"
    }
})
