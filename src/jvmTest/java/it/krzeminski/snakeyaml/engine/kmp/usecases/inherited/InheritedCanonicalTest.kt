package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token
import java.io.InputStream
import java.nio.file.Files

class InheritedCanonicalTest: FunSpec({
    test("Canonical scan") {
        val files = getStreamsByExtension(".canonical")
        files.size shouldBeGreaterThan 0
        files.forEach { file ->
            Files.newInputStream(file.toPath()).use { input ->
                val tokens = canonicalScan(input, file.name)
                tokens.shouldNotBeEmpty()
            }
        }
    }

    test("Canonical parse") {
        val files = getStreamsByExtension(".canonical")
        files.size shouldBeGreaterThan 0
        files.forEach { file ->
            Files.newInputStream(file.toPath()).use { input ->
                val tokens = canonicalParse(input, file.name)
                tokens.shouldNotBeEmpty()
            }
        }
    }
})

private fun canonicalScan(input: InputStream, label: String): List<Token> {
    val buffer = buildString {
        var ch = input.read()
        while (ch != -1) {
            append(ch.toChar())
            ch = input.read()
        }
    }
    val scanner = CanonicalScanner(buffer.toString().replace(System.lineSeparator(), "\n"), label)
    return buildList {
        while (scanner.hasNext()) {
            add(scanner.next())
        }
    }
}
