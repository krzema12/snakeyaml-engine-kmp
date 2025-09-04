package it.krzeminski.snakeyaml.engine.kmp.usecases.fuzzy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ScannerException

/**
 * https://github.com/FasterXML/jackson-dataformats-text/issues/406
 * https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=56902
 */
class FuzzYAMLRead56902Test : FunSpec({
    test("huge minor value") {
        val yamlProcessor = Load()
        shouldThrow<ScannerException> {
            yamlProcessor.loadOne("%YAML 1.9224775801")
        }.also { exception ->
            exception.message shouldContain "found a number which cannot represent a valid version: 9224775801"
        }
    }

    test("huge major value") {
        val yamlProcessor = Load()
        shouldThrow<ScannerException> {
            yamlProcessor.loadOne("%YAML 100651234565.1")
        }.also { exception ->
            exception.message shouldContain "found a number which cannot represent a valid version: 100651234565"
        }
    }
})
