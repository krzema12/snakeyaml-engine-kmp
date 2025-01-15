package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ReaderException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.source
import java.io.FileInputStream

class InheritedReaderTest: FunSpec({
    test("Reader errors") {
        val inputs = getStreamsByExtension(".stream-error")
        inputs
            // Skip these files - Okio seems to parse them correctly, so the test fails.
            // Supporting UTF-16 will be much more difficult anyway as more code is transferred to KMP, because
            // KMP basically only supports UTF-8.
            .filter { it.name !in setOf("odd-utf16.stream-error", "invalid-utf8-byte.stream-error") }
            .forEach { file ->
                val input = FileInputStream(file)
                val unicodeReader = YamlUnicodeReader(input.source())
                val stream = StreamReader(LoadSettings.builder().build(), unicodeReader)
                try {
                    while (stream.peek() != 0) {
                        stream.forward()
                    }
                    fail("Invalid stream must not be accepted: ${file.absolutePath}; encoding=${unicodeReader.encoding}")
                } catch (e: ReaderException) {
                    e.toString() shouldContain " special characters are not allowed"
                } catch (e: YamlEngineException) {
                    e.toString() shouldContain " MalformedInputException"
                } finally {
                    input.close()
            }
        }
    }
})
