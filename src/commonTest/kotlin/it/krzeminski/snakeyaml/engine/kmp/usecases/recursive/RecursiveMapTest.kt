@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.usecases.recursive

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class RecursiveMapTest : FunSpec({
    test("load map with recursive values") {
        val load = Load()
        val map = load.loadOne(
            "First occurrence: &anchor Foo\n" +
                    "Second occurrence: *anchor\n" +
                    "Override anchor: &anchor Bar\n" +
                    "Reuse anchor: *anchor\n"
        ) as Map<String, String>

        val expected = mapOf(
            "First occurrence" to "Foo",
            "Second occurrence" to "Foo",
            "Override anchor" to "Bar",
            "Reuse anchor" to "Bar"
        )
        map shouldBe expected
    }

    test("dump and load map with recursive values") {
        val map1 = mutableMapOf<String, Any>("name" to "first")
        val map2 = mutableMapOf<String, Any>("name" to "second")
        map1["next"] = map2
        map2["next"] = map1

        val dump = Dump(DumpSettings.builder().build())
        val output1 = dump.dumpToString(map1)
        output1 shouldBe "&id001\nname: first\nnext:\n  name: second\n  next: *id001\n"

        val load = Load()
        val parsed1 = load.loadOne(output1) as Map<String, Any>
        parsed1.size shouldBe 2
        parsed1["name"] shouldBe "first"
        val parsed2 = parsed1["next"] as Map<String, Any>
        parsed2["name"] shouldBe "second"
    }

    test("fail to load map with recursive keys") {
        val load = Load()
        // fail to load map which has only one key - reference to itself
        shouldThrowWithMessage<YamlEngineException>(message = "Recursive key for mapping is detected but it is not configured to be allowed.") {
            load.loadOne("&id002\n*id002 : foo")
        }
    }

    test("load map with recursive keys if it is explicitly allowed") {
        val settings = LoadSettings.builder().setAllowRecursiveKeys(true).build()
        val load = Load(settings)
        // load map which has only one key - reference to itself
        val recursive = load.loadOne("&id002\n*id002 : foo") as Map<Any, Any>
        recursive.size shouldBe 1
    }
})
