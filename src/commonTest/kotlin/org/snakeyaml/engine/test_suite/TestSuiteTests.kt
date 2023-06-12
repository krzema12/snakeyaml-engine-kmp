package org.snakeyaml.engine.test_suite

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.snakeyaml.engine.test_suite.SuiteUtils.deviationsWithError
import org.snakeyaml.engine.test_suite.SuiteUtils.deviationsWithSuccess
import org.snakeyaml.engine.test_suite.SuiteUtils.isIgnored


class TestSuiteTests : FunSpec({
    test("expect correct number of test suites have been generated") {
        val testSuites = yamlTestSuiteData

        testSuites shouldHaveSize 402
    }

    yamlTestSuiteData.forEach { (id, data) ->
        test("expect all test data can be read - $id") {
            withClue(data) {
                data.label.shouldNotBeEmpty()
                when (data) {
                    is YamlTestData.Error   -> {}
                    is YamlTestData.Success -> {
                        data.testEvent.shouldNotBeEmpty()
                    }
                }
            }
        }
    }

    yamlTestSuiteData.forEach { (id, data) ->
        val result = SuiteUtils.parseData(data)

        if (data.isIgnored()) {
            test("expect test $id is ignored because it deviates from the expected result") {
                when (id) {
                    in deviationsWithError   ->
                        withClue("Expected error, but got none") {
                            result.error shouldNotBe null
                        }

                    else ->
                        withClue("Expected no error, but got ${result.error}") {
                            result.error shouldBe null
                        }
                }
            }
        } else {
            when (data) {
                is YamlTestData.Error   ->
                    test("expect test $id cannot be parsed - ${data.label}") {
                        withClue("Expected error, but got none") {
                            result.error shouldNotBe null
                        }
                    }

                is YamlTestData.Success ->
                    test("expect test $id can be parsed - ${data.label}") {
                        withClue("Expected no error, but got ${result.error}") {
                            result.error shouldBe null
                        }

                        val expectedEvents = data.testEvent.lines().joinToString("\n") { it.trim() }.trim()
                        val actualEvents = result.events.joinToString("\n") { it.toString().trim() }.trim()

                        actualEvents shouldBe expectedEvents
                    }
            }
        }
    }
})
