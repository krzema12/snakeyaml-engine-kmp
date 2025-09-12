package it.krzeminski.snakeyaml.engine.kmp.test_suite

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import it.krzeminski.snakeyaml.engine.kmp.test_suite.SuiteUtils.deviationsInEvents
import it.krzeminski.snakeyaml.engine.kmp.test_suite.SuiteUtils.deviationsInResult


class TestSuiteTests : FunSpec({
    test("expect correct number of test suites have been generated") {
        YamlTestSuiteData shouldHaveSize 402
    }

    YamlTestSuiteData.forEach { (id, data) ->
        test("expect all test data can be read - $id") {
            withClue(data) {
                data.label.shouldNotBeEmpty()
                data.testEvent.shouldNotBeEmpty()
            }
        }
    }

    YamlTestSuiteData.forEach { (id, data) ->
        val result = SuiteUtils.parseData(data)

        when (id) {
            in deviationsInResult ->
                when (data) {
                    is YamlTestData.Error   ->
                        test("expect test ${data.id} is ignored because it succeeds, but should fail") {
                            assertSoftly {
                                withClue("Expected result did not have an error, but it did") {
                                    result.error shouldBe null
                                }
                                result shouldNotHaveEvents data.testEvent
                            }
                        }

                    is YamlTestData.Success ->
                        test("expect test ${data.id} is ignored because it fails, but should succeed") {
                            assertSoftly {
                                withClue("Expected result had an error, but got none") {
                                    result.error shouldNotBe null
                                }
                                result shouldNotHaveEvents data.testEvent
                            }
                        }
                }

            in deviationsInEvents ->
                when (data) {
                    is YamlTestData.Error   ->
                        test("expect test ${data.id} is ignored because it fails, but with wrong events emitted") {
                            assertSoftly {
                                withClue("Expected error, but got none") {
                                    result.error shouldNotBe null
                                }
                                result shouldNotHaveEvents data.testEvent
                            }
                        }

                    is YamlTestData.Success ->
                        test("expect test ${data.id} is ignored because it succeeds, but with wrong events emitted") {
                            assertSoftly {
                                withClue("Expected no error, but got ${result.error}") {
                                    result.error shouldBe null
                                }
                                result shouldNotHaveEvents data.testEvent
                            }
                        }
                }

            else                  -> {
                when (data) {
                    is YamlTestData.Error   ->
                        test("expect test $id cannot be parsed - ${data.label}") {
                            assertSoftly {
                                withClue("Expected error, but got none") {
                                    result.error shouldNotBe null
                                }
                                result shouldHaveEvents data.testEvent
                            }
                        }

                    is YamlTestData.Success ->
                        test("expect test $id can be parsed - ${data.label}") {
                            assertSoftly {
                                withClue("Expected no error, but got ${result.error}") {
                                    result.error shouldBe null
                                }
                                result shouldHaveEvents data.testEvent
                            }
                        }
                }
            }
        }
    }

    deviationsInResult.forEach { id ->
        test("validate deviation in result $id") {
            id shouldBeIn YamlTestSuiteData.keys
        }
    }

    deviationsInEvents.forEach { id ->
        test("validate deviation in events $id") {
            id shouldBeIn YamlTestSuiteData.keys
        }
    }
})

private infix fun ParseResult.shouldHaveEvents(expectedEvents: String) {
    val actualEvents = events.joinToString("\n")
    actualEvents shouldBe expectedEvents.normalizeLineEndings()

}

private infix fun ParseResult.shouldNotHaveEvents(expectedEvents: String) {
    val actualEvents = events.joinToString("\n")
    actualEvents shouldNotBe expectedEvents.normalizeLineEndings()
}

private fun String.normalizeLineEndings(): String = lines().joinToString("\n").trim()
