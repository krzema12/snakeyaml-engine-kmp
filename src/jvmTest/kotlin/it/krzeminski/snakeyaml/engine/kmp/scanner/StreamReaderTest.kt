package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class StreamReaderTest : FunSpec({
    val loadSettings = LoadSettings()
    fun reader(stream: String): StreamReader = StreamReader(loadSettings, stream)

    test("test peeking and forwarding") {
        val reader = reader(stream = "abc")

        shouldThrow<IndexOutOfBoundsException> { reader.peek(-1) }
        reader.peek(0) shouldBe 'a'.code
        reader.peek(1) shouldBe 'b'.code
        reader.peek(2) shouldBe 'c'.code
        reader.peek(3) shouldBe 0

        reader.shouldHave(index = 0, documentIndex = 0, line = 0, column = 0)

        reader.forward()

        reader.peek(-1) shouldBe 'a'.code
        reader.peek(0) shouldBe 'b'.code
        reader.peek(1) shouldBe 'c'.code
        reader.peek(2) shouldBe 0

        reader.shouldHave(index = 1, documentIndex = 1, line = 0, column = 1)

        reader.forward(2)

        reader.peek(-1) shouldBe 'c'.code
        reader.peek(0) shouldBe 0

        reader.shouldHave(index = 3, documentIndex = 3, line = 0, column = 3)
    }

    test("when forwarding past index, indices should be limited to max input index") {
        val reader = reader(stream = "abc")
        reader.forward(10)
        reader.shouldHave(index = 3, documentIndex = 3, line = 0, column = 3)
    }

    test("line incrementing") {
        val reader = reader(stream = "\n\n\n\n")
        reader.shouldHave(index = 0, documentIndex = 0, line = 0, column = 0)

        reader.forward()

        reader.peek() shouldBe '\n'.code
        reader.shouldHave(index = 1, documentIndex = 1, line = 1, column = 0)

        reader.forward(2)
        reader.peek() shouldBe '\n'.code
        reader.shouldHave(index = 3, documentIndex = 3, line = 3, column = 0)
    }
})

private fun StreamReader.shouldHave(
    index: Int,
    documentIndex: Int,
    line: Int,
    column: Int,
) {
    withClue(
        "StreamReader(index=${this.index},documentIndex=${this.documentIndex},line=${this.line},column=${this.column})"
    ) {
        withClue("index") { this.index shouldBe index }
        withClue("documentIndex") { this.documentIndex shouldBe documentIndex }
        withClue("line") { this.line shouldBe line }
        withClue("column") { this.column shouldBe column }

        getMark().asClue { mark ->
            mark.shouldNotBeNull()
            withClue("index") { mark.index shouldBe index }
            withClue("line") { mark.line shouldBe line }
            withClue("column") { mark.column shouldBe column }
        }
    }
}
