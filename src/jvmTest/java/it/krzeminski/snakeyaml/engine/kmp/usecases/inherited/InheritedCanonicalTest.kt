package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token
import okio.BufferedSource
import okio.buffer

class InheritedCanonicalTest: FunSpec({
    test("Canonical scan") {
        val files = getStreamsByExtension(".canonical")
        files.size shouldBeGreaterThan 0
        files.forEach { file ->
            file.source().use { input ->
                val tokens = canonicalScan(input.buffer(), file.name)
                tokens.shouldNotBeEmpty()
            }
        }
    }

    test("Canonical parse") {
        val files = getStreamsByExtension(".canonical")
        files.size shouldBeGreaterThan 0
        files.forEach { file ->
            file.source().use { input ->
                val tokens = canonicalParse(input, file.name)
                tokens.shouldNotBeEmpty()
            }
        }
    }
})

private fun canonicalScan(input: BufferedSource, label: String): List<Token> {
    val buffer = buildString {
        while (input.exhausted()) {
            var ch = input.readByte().toInt()
            append(ch.toChar())
        }
    }
    val scanner = CanonicalScanner(buffer.toString().replace(System.lineSeparator(), "\n"), label)
    return buildList {
        while (scanner.hasNext()) {
            add(scanner.next())
        }
    }
}
