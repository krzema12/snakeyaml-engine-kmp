package it.krzeminski.snakeyaml.engine.kmp.events

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark

class EventTest : FunSpec({
    test("testToString") {
        val alias = AliasEvent(Anchor("111"))
        alias shouldNotBe alias.toString()
    }

    test("bothMarks") {
        val fake = Mark("a", 0, 0, 0, emptyList(), 0)
        shouldThrow<NullPointerException> {
            StreamStartEvent(null, fake)
        }.also { exception ->
            exception.message shouldBe "Both marks must be either present or absent."
        }
    }
})
