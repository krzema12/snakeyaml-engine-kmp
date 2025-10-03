@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.api.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter
import java.util.Optional

/**
 * This test needs to be in the jvmTest source set because Optional is a Java-specific type
 * that cannot be replaced with Kotlin API in multiplatform code.
 */
class OptionalTest : FunSpec({
    test("represent Optional as value") {
        val standardRepresenter = StandardRepresenter(DumpSettings.builder().build())
        val node = standardRepresenter.represent(Optional.of("a"))
        node.tag.value shouldBe "tag:yaml.org,2002:java.util.Optional"
    }

    test("represent Optional.empty as null") {
        val standardRepresenter = StandardRepresenter(DumpSettings.builder().build())
        val node = standardRepresenter.represent(Optional.empty<Any>())
        node.tag.value shouldBe "tag:yaml.org,2002:null"
    }

    test("dump Optional as its value") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(Optional.of("a"))
        str shouldBe "!!java.util.Optional 'a'\n"
    }

    test("dump empty Optional as null") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(Optional.empty<Any>())
        str shouldBe "null\n"
    }

    test("dump Optionals in list") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(listOf(
            Optional.of(2),
            Optional.empty<Any>(),
            Optional.of("a")
        ))
        str shouldBe "[!!java.util.Optional '2', null, !!java.util.Optional 'a']\n"
    }

    test("dump Optional containing list") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val str = dump.dumpToString(Optional.of(listOf(1, 2)))
        str shouldBe "!!java.util.Optional [1, 2]\n"
    }

    test("Optional 'a' is parsed") {
        val load = Load()
        val str = load.loadOne("!!java.util.Optional a") as Optional<String>
        str shouldBe Optional.of("a")
    }

    test("empty Optional with null parsed") {
        val load = Load()
        val str = load.loadOne("!!java.util.Optional null") as Optional<String>
        str shouldBe Optional.empty<String>()
    }

    test("empty Optional with empty string parsed") {
        val load = Load()
        val str = load.loadOne("!!java.util.Optional ") as Optional<String>
        str shouldBe Optional.empty<String>()
    }
})
