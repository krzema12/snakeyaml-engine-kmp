package it.krzeminski.snakeyaml.engine.kmp.issues.issue54

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

/**
 * Issue 54: add a space after anchor (when it is a simple key)
 */
class DumpWithoutSpaceTest : FunSpec({
    fun parse(data: String): Any? {
        val loadSettings = LoadSettings.builder().setAllowRecursiveKeys(true).build();
        val load = Load(loadSettings);
        return load.loadOne(data);
    }

    test("The document does not have a space after the *1 alias") {
        shouldThrow<Exception> {
            parse(
                """|--- &1
                   |hash:
                   |  :one: true
                   |  :two: true
                   |  *1: true""".trimMargin()
            )
        }.also {
            it.message shouldContain "could not find expected ':'"
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
        obj.shouldNotBeNull()
    }

    test("Dump and load an alias") {
        val map = mutableMapOf<Any, Boolean>(
            ":one" to true,
        )
        map[map] = true
        val dumpSettings = DumpSettings.builder().build()
        val dump = Dump(dumpSettings)
        val output = dump.dumpToString(map)
        output shouldBe """|&id001
                           |:one: true
                           |*id001 : true
                           |""".trimMargin()
        val recursive = parse(output)
        recursive.shouldNotBeNull()
    }
})
