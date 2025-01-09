package it.krzeminski.snakeyaml.engine.kmp.issues.issue149

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class GlobalDirectivesTest : FunSpec({
    fun yamlToEvents(resourceName: String): Iterable<Event> {
        val input = stringFromResources(resourceName)
        val parser = Parse(LoadSettings.builder().build())
        return parser.parse(input)
    }

    test("Use tag directive") {
        val events = yamlToEvents("issues/issue149-one-document.yaml")
        events.toList().size shouldBe 10
    }

    test("Fail to parse because directive does not stay for the second document") {
        val events = yamlToEvents("issues/issue149-losing-directives.yaml")
        shouldThrow<ParserException> {
            events.toList()
        }.also {
            it.message shouldContain "found undefined tag handle !u!"
        }
    }

    test("Parse both tag directives") {
        val events = yamlToEvents("issues/issue149-losing-directives-2.yaml")
        events.toList().size shouldBe 18
    }
})
