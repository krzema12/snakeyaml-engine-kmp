@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.usecases.custom_collections

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class CustomDefaultCollectionsTest : FunSpec({
    test("create LinkedList by default") {
        // init size is not used in LinkedList
        val settings = LoadSettings(defaultList = { mutableListOf() })
        val load = Load(settings)
        val list = load.loadOne("- a\n- b") as List<String>
        list shouldHaveSize 2
    }

    test("create TreeMap by default") {
        // init size is not used in TreeMap
        val settings = LoadSettings(defaultMap = { mutableMapOf() })
        val load = Load(settings)
        val map = load.loadOne("{k1: v1, k2: v2}")
        map.shouldBeInstanceOf<Map<String, String>>()
        map shouldHaveSize 2
    }

    test("create TreeSet by default") {
        val settings = LoadSettings(defaultSet = { mutableSetOf() })
        val load = Load(settings)
        val set = load.loadOne("!!set\n? foo\n? bar")
        set.shouldBeInstanceOf<Set<String>>()
        set shouldHaveSize 2
        // must be re-ordered
        val sortedSet = set.sorted()
        sortedSet.first() shouldBe "bar"
        sortedSet.last() shouldBe "foo"
    }
})
