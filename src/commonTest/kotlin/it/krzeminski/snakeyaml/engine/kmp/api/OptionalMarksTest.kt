package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException
import it.krzeminski.snakeyaml.engine.kmp.test_suite.DATA_2AUY
import it.krzeminski.snakeyaml.engine.kmp.test_suite.YamlTestSuiteData

class OptionalMarksTest: FunSpec({
    test("Compose: no marks") {
        val data = YamlTestSuiteData[DATA_2AUY.id]!!
        val settings = LoadSettings.builder()
            .setLabel(data.label)
            .setUseMarks(false)
            .build()
        val node = Compose(settings).compose("{a: 4}")
        node.shouldNotBeNull()
    }

    test("Compose: failure with marks") {
        val data = YamlTestSuiteData[DATA_2AUY.id]!!
        val settings = LoadSettings.builder()
            .setLabel(data.label)
            .setUseMarks(true)
            .build()
        shouldThrow<ParserException> {
            Compose(settings).compose("{a: 4}}")
        }.also {
            withClue("The error must contain Mark data.") {
                it.message shouldContain "line 1, column 7:"
            }
        }
    }

    test("Compose: failure without marks") {
        val data = YamlTestSuiteData[DATA_2AUY.id]!!
        val settings = LoadSettings.builder()
            .setLabel(data.label)
            .setUseMarks(false)
            .build()
        shouldThrow<ParserException> {
            Compose(settings).compose("{a: 4}}")
        }.also {
            withClue("The error must contain Mark data.") {
                it.message shouldBe "expected '<document start>', but found '}'\n"
            }
        }
    }
})
