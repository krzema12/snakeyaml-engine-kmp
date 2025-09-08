package it.krzeminski.snakeyaml.engine.kmp.events

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark

class EventTest : FunSpec({
    test("toString") {
        val alias = AliasEvent(Anchor("111"))
        alias shouldNotBe alias.toString()
    }

    test("both marks") {
        val fake = Mark("a", 0, 0, 0, emptyList(), 0)
        shouldThrowWithMessage<NullPointerException>(message = "Both marks must be either present or absent.") {
            StreamStartEvent(startMark = null, endMark = fake)
        }
    }
})
