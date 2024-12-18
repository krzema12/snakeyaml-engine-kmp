package it.krzeminski.snakeyaml.engine.kmp.issues.issue54

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class DumpWithoutSpaceTest : FunSpec({
    fun parse(data: String): Any? {
        val loadSettings = LoadSettings.builder().setAllowRecursiveKeys(true).build();
        val load = Load(loadSettings);
        return load.loadOne(data);
    }

    test("The output does not include a space after the *1 alias") {
        try {
            val obj = parse(
                """|--- &1
                   |hash:
                   |  :one: true
                   |  :two: true
                   |  *1: true""".trimMargin()
            )
            obj shouldNotBe null
        } catch (e: Exception) {
            e.message shouldContain "could not find expected ':'"
        }
    }

    test("The output does include a space after the *1 alias") {
        val obj = parse(
            """|--- &1
               |hash:
               |  :one: true
               |  :two: true
               |  *1 : true""".trimMargin()
        )
        obj shouldNotBe null
    }

    test("Dump and load an alias") {
        val map = mutableMapOf<Any, Boolean>(
            ":one" to true,
        )
        map[map] = true
        val dumpSettings = DumpSettings.builder().build()
        val dump = Dump(dumpSettings)
        val output = dump.dumpToString(map)
        try {
            parse(output)
        } catch (e: Exception) {
            e.message shouldContain "could not find expected ':'"
        }
    }
})
