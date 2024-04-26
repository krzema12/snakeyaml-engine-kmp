package it.krzeminski.snakeyaml.engine.kmp.test_suite

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import it.krzeminski.snakeyaml.engine.kmp.test_suite.SuiteUtils.deviationsWithError
import it.krzeminski.snakeyaml.engine.kmp.test_suite.SuiteUtils.deviationsWithSuccess


class TestSuiteTests : FunSpec({
    test("expect correct number of test suites have been generated") {
        YamlTestSuiteData shouldHaveSize 402
    }

    YamlTestSuiteData.forEach { (id, data) ->
        test("expect all test data can be read - $id") {
            withClue(data) {
                data.label.shouldNotBeEmpty()
                if (data is YamlTestData.Success) {
                    data.testEvent.shouldNotBeEmpty()
                }
            }
        }
    }

    YamlTestSuiteData.forEach { (id, data) ->
        val result = SuiteUtils.parseData(data)

        when (id) {
            in deviationsWithError   ->
                test("expect test ${data.id} is ignored because it fails, but should succeed") {
                    withClue("Expected result had an error, but got none") {
                        result.error shouldNotBe null
                    }
                }

            in deviationsWithSuccess ->
                test("expect test ${data.id} is ignored because succeeds, but it should fail") {
                    withClue("Expected result did not have an error, but it did") {
                        result.error shouldBe null
                    }
                    if (data is YamlTestData.Success) {
                        result shouldNotHaveEvents data.testEvent
                    }
                }

            else                     -> {
                // use if-statement instead of when-statement because of https://youtrack.jetbrains.com/issue/KT-59274
                if (data is YamlTestData.Error) test("expect test $id cannot be parsed - ${data.label}") {
                    withClue("Expected error, but got none") {
                        result.error shouldNotBe null
                    }
                } else if (data is YamlTestData.Success) test("expect test $id can be parsed - ${data.label}") {
                    withClue("Expected no error, but got ${result.error}") {
                        result.error shouldBe null
                    }
                    result shouldHaveEvents data.testEvent
                }
            }
        }
    }
})

private infix fun ParseResult.shouldHaveEvents(expectedEvents: String) {
    val actualEvents = events?.joinToString("\n")
    actualEvents shouldBe expectedEvents.normalizeLineEndings()

}

private infix fun ParseResult.shouldNotHaveEvents(expectedEvents: String) {
    val actualEvents = events?.joinToString("\n")
    actualEvents shouldNotBe expectedEvents.normalizeLineEndings()
}

private fun String.normalizeLineEndings(): String = lines().joinToString("\n").trim()
