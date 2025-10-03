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

/**
 * Example of parsing a local tag
 */
class LocalTagTest : FunSpec({
    test("local tag") {
        // register to call CustomConstructor when the Tag !ImportValue is found
        val tagConstructors = mapOf(Tag("!ImportValue") to CustomConstructor())
        val settings = LoadSettings.builder().setTagConstructors(tagConstructors).build()
        val loader = Load(settings)
        val obj = loader.loadOne("VpcId: !ImportValue SpokeVPC")
        val map = obj as Map<String, ImportValueImpl>
        map["VpcId"]!!.value shouldBe "SpokeVPC"
    }
})

/**
 * Create ImportValueImpl from a scalar node
 */
private class CustomConstructor : ConstructNode {
    override fun construct(node: Node?): Any {
        val scalar = node as ScalarNode
        return ImportValueImpl(scalar.value)
    }
}

/**
 * Business value to be parsed from YAML
 */
private class ImportValueImpl(val value: String)
