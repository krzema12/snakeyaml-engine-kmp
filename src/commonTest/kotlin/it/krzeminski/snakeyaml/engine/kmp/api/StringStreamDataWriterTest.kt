package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class StringStreamDataWriterTest: FunSpec({
    test("toString() doesn't consume buffer") {
        val writer = StringStreamDataWriter()
        writer.write("foobar")

        // First read.
        writer.toString()

        withClue("subsequent calls to toString() shouldn't change the buffer's state") {
            writer.toString() shouldContain "foobar"
        }
    }
})
