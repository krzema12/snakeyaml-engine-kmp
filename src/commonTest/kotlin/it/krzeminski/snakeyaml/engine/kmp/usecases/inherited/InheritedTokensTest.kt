package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import io.kotest.assertions.AssertionErrorBuilder.Companion.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.scanner.ScannerImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import it.krzeminski.snakeyaml.engine.kmp.tokens.StreamEndToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.StreamStartToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token
import okio.use

class InheritedTokensTest: FunSpec({
    test("Tokens are correct") {
        val replaces = mapOf(
            Token.ID.Directive to "%",
            Token.ID.DocumentStart to "---",
            Token.ID.DocumentEnd to "...",
            Token.ID.Alias to "*",
            Token.ID.Anchor to "&",
            Token.ID.Tag to "!",
            Token.ID.Scalar to "_",
            Token.ID.BlockSequenceStart to "[[",
            Token.ID.BlockMappingStart to "{{",
            Token.ID.BlockEnd to "]}",
            Token.ID.FlowSequenceStart to "[",
            Token.ID.FlowSequenceEnd to "]",
            Token.ID.FlowMappingStart to "{",
            Token.ID.FlowMappingEnd to "}",
            Token.ID.BlockEntry to ",",
            Token.ID.FlowEntry to ",",
            Token.ID.Key to "?",
            Token.ID.Value to ":"
        )
        val tokensFiles = getStreamsByExtension(".tokens")
        tokensFiles.size shouldBeGreaterThan 0
        tokensFiles.forEach { tokenFile ->
            val position = tokenFile.name.lastIndexOf('.')
            val dataName = tokenFile.name.substring(0, position) + ".data"

            val tokenFileData = getResource(tokenFile.name)
                // This is needed because Java's split omits the last empty line -
                // looks like Kotlin's implementation is correct.
                .trimEnd()
            val split = tokenFileData.split(Regex("""\s+"""))
            val tokens2 = buildList {
                addAll(split)
            }

            val tokens1 = mutableListOf<String>()
            val settings = LoadSettings()
            getFileByName(dataName).source().use { source ->
                val reader = StreamReader(
                    loadSettings = settings,
                    stream = YamlUnicodeReader(source),
                )
                val scanner = ScannerImpl(settings, reader)
                try {
                    while (scanner.checkToken()) {
                        val token = scanner.next()
                        if (!(token is StreamStartToken || token is StreamEndToken)) {
                            val replacement = replaces[token.tokenId]!!
                            tokens1.add(replacement)
                        }
                    }
                    tokens1 shouldBe tokens2
                } catch (_: RuntimeException) {
                    println("File name: \n${tokenFile.name}")
                    val data = getResource(tokenFile.name)
                    println("Data: \n$data")
                    println("Tokens:")
                    tokens1.forEach {
                        println(it)
                    }
                    fail("Cannot scan: $tokenFile")
                }
            }
        }
    }

    test("Tokens are correct in data files") {
        val files = getStreamsByExtension(".data", onlyIfCanonicalPresent = true)
        files.size shouldBeGreaterThan 0
        files.forEach { file ->
            val tokens = mutableListOf<String>()
            file.source().use { source ->
                val settings = LoadSettings()
                val reader = StreamReader(settings, YamlUnicodeReader(source))
                val scanner = ScannerImpl(settings, reader)
                try {
                    while (scanner.checkToken()) {
                        val token = scanner.next()
                        tokens.add(token::class.toString())
                    }
                } catch (e: RuntimeException) {
                    println("File name: \n${file.name}")
                    val data = getResource(file.name)
                    println("Data: \n$data")
                    println("Tokens:")
                    tokens.forEach {
                        println(it)
                    }
                    fail("Cannot scan: $file; ${e.message}")
                }
            }
        }
    }
})
