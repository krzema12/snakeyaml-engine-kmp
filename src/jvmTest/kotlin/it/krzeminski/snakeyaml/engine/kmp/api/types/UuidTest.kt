package it.krzeminski.snakeyaml.engine.kmp.api.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter
import java.util.UUID

/**
 * This test needs to be in the jvmTest source set because UUID is a Java-specific type
 * that cannot be replaced with Kotlin API in multiplatform code.
 */
class UuidTest : FunSpec({
    test("Represent UUID as node with global tag") {
        val standardRepresenter = StandardRepresenter(DumpSettings.builder().build())
        val node = standardRepresenter.represent(THE_UUID)
        node.tag.value shouldBe "tag:yaml.org,2002:java.util.UUID"
    }

    test("Dump UUID as string") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val output = dump.dumpToString(THE_UUID)
        output shouldBe "!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n"
    }

    test("Parse UUID") {
        val load = Load()
        val uuid = load.loadOne("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n") as UUID
        uuid shouldBe THE_UUID
    }

    test("Parse UUID as root") {
        val load = Load()
        val uuid = load.loadOne("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n") as UUID
        uuid shouldBe THE_UUID
    }
})

private val THE_UUID = UUID.fromString("37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc")
