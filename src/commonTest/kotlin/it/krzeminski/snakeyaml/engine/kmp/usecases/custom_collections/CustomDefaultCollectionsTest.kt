package it.krzeminski.snakeyaml.engine.kmp.usecases.custom_collections

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class CustomDefaultCollectionsTest : FunSpec({
    test("create LinkedList by default") {
        // init size is not used in LinkedList
        val settings = LoadSettings.builder().setDefaultList { mutableListOf() }.build()
        val load = Load(settings)
        val list = load.loadOne("- a\n- b") as MutableList<String>
        list.size shouldBe 2
    }

    test("create TreeMap by default") {
        // init size is not used in TreeMap
        val settings = LoadSettings.builder().setDefaultMap { mutableMapOf() }.build()
        val load = Load(settings)
        val map = load.loadOne("{k1: v1, k2: v2}")
        map.shouldBeInstanceOf<Map<String, String>>()
        map.size shouldBe 2
    }

    test("create TreeSet by default") {
        val settings = LoadSettings.builder().setDefaultSet { mutableSetOf() }.build()
        val load = Load(settings)
        val set = load.loadOne("!!set\n? foo\n? bar")
        set.shouldBeInstanceOf<Set<String>>()
        set.size shouldBe 2
        // must be re-ordered
        val sortedSet = set.sorted()
        sortedSet.first() shouldBe "bar"
        sortedSet.last() shouldBe "foo"
    }
})
