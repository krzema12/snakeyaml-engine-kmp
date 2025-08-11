package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.schema.FailsafeSchema
import it.krzeminski.snakeyaml.engine.kmp.schema.Schema

class NoScalarResolverTest : FunSpec({
    test("resolve with FailsafeSchema") {
        val settings = LoadSettings.builder().setSchema(FailsafeSchema()).build()
        val load = Load(settings)
        val str = load.loadOne("5")
        str shouldBe "5"
    }

    test("resolve with custom ScalarResolver") {
        val schema = object : Schema {
            override val scalarResolver: ScalarResolver
                get() = MyScalarResolver()

            override val schemaTagConstructors: Map<Tag, ConstructNode>
                get() = mapOf()
        }
        val settings = LoadSettings.builder().setSchema(schema).build()
        val load = Load(settings)
        val str = load.loadOne("5")
        str shouldBe "5"
    }
})

private class MyScalarResolver : ScalarResolver {
    override fun resolve(value: String, implicit: Boolean): Tag =
        Tag.STR
}
