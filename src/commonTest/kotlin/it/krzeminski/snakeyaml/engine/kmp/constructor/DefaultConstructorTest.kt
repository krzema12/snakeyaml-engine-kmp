package it.krzeminski.snakeyaml.engine.kmp.constructor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.string.shouldStartWith
import io.kotest.assertions.throwables.shouldThrow
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

class DefaultConstructorTest : FunSpec({

    test("construct null when unknown") {
        val settings = LoadSettings.builder().build()
        val load = Load(settings, MagicNullConstructor(settings))
        val str = load.loadOne("!unknownLocalTag a") as? String
        str.shouldBeNull()
    }

    test("fail when unknown") {
        val load = Load()
        shouldThrow<YamlEngineException> {
            load.loadOne("!unknownLocalTag a")
        }.also {
            it.message shouldStartWith "could not determine a constructor for the tag !unknownLocalTag"
        }
    }
})

/**
 * Make NULL if the tag is not recognized
 */
private class MagicNullConstructor(settings: LoadSettings) : StandardConstructor(settings) {
    override fun findConstructorFor(node: Node): ConstructNode {
        return ConstructYamlNull()
    }
}
