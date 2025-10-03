@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.usecases.tags

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.BaseScalarResolver
import it.krzeminski.snakeyaml.engine.kmp.resolver.JsonScalarResolver
import it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema
import kotlinx.datetime.LocalDateTime

/**
 * Example of parsing a !!timestamp tag
 */
class TimestampTagTest : FunSpec({
    test("explicit tag") {
        val tagConstructors = mapOf(myTimeTag to TimestampConstructor())
        val settings = LoadSettings.builder().setTagConstructors(tagConstructors).build()
        val loader = Load(settings)
        val obj = loader.loadOne("!!timestamp 2020-03-24T12:34:00.333") as LocalDateTime
        obj shouldBe LocalDateTime(2020, 3, 24, 12, 34, 0, 333000000)
    }

    test("implicit tag") {
        val settings = LoadSettings.builder().setSchema(TimestampSchema()).build()
        val loader = Load(settings)
        val obj = loader.loadOne("2020-03-24T12:34:00.333") as LocalDateTime
        obj shouldBe LocalDateTime(2020, 3, 24, 12, 34, 0, 333000000)
    }

    test("implicit tag in map") {
        val settings = LoadSettings.builder().setSchema(TimestampSchema()).build()
        val loader = Load(settings)
        val map = loader.loadOne("time: 2020-03-24T13:44:10.333") as Map<String, LocalDateTime>
        val time = map["time"]!!
        time shouldBe LocalDateTime(2020, 3, 24, 13, 44, 10, 333000000)
    }
})

// this is an example of the tag from YAML 1.1 spec. It can be anything else
private val myTimeTag = Tag("${Tag.PREFIX}timestamp")

private class TimestampConstructor : ConstructNode {
    override fun construct(node: Node?): Any {
        val scalar = node as ScalarNode
        // the parsing depends on what should be parsed and to which object
        // examples can be found in SnakeYAML tests for the YAML 1.1 types format
        return LocalDateTime.parse(scalar.value)
    }
}

/**
 * This is required to support implicit tags
 */
private class MyScalarResolver : BaseScalarResolver() {
    private val delegate = JsonScalarResolver()

    // this is taken from YAML 1.1 types
    // it can be changed to represent the business case
    companion object {
        private val TIMESTAMP = Regex(
            "^(?:[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]?-[0-9][0-9]?(?:[Tt]|[ \t]+)[0-9][0-9]?:[0-9][0-9]:[0-9][0-9](?:\\.[0-9]*)?(?:[ \t]*(?:Z|[-+][0-9][0-9]?(?::[0-9][0-9])?))?)$"
        )
    }

    override fun resolve(value: String, implicit: Boolean): Tag {
        return if (TIMESTAMP.matches(value)) {
            myTimeTag
        } else {
            delegate.resolve(value, implicit)
        }
    }
}

private class TimestampSchema : JsonSchema(MyScalarResolver()) {
    override val schemaTagConstructors: Map<Tag, ConstructNode>
        get() {
            val parent = super.schemaTagConstructors.toMutableMap()
            parent[myTimeTag] = TimestampConstructor()
            return parent
        }
}
