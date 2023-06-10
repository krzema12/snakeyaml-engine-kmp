package org.snakeyaml.engine.test_suite

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.snakeyaml.engine.test_suite.SuiteUtils.isIgnored


class TestSuiteTests : FunSpec({
    test("expect correct number of test suites have been generated") {
        val testSuites = yamlTestSuiteData

        testSuites shouldHaveSize 351
    }

    yamlTestSuiteData.forEach { (id, data) ->
        test("expect all test data can be read - $id") {
            withClue(data) {
                SuiteUtils.readData(id, data)
            }
        }
    }

    SuiteUtils.all().forEach { data ->
        if (data.isIgnored()) {
            xtest("suite ${data.suiteId} is ignored")
        } else {

            SuiteUtils.parseData(data).entries.forEach { (case, result) ->
                when {
                    case.skip        -> {
                        xtest("case ${case.caseId} is skipped")
                    }

                    case.isIgnored() -> {
                        xtest("case ${case.caseId} is ignored")
                    }

                    case.fail        -> {
                        test("expect case ${case.caseId} cannot be parsed - ${data.name}") {
                            withClue("Expected error, but got none") {
                                result.error shouldNotBe null
                            }
                        }
                    }

                    else             -> {
                        test("expect case ${case.caseId} can be parsed - ${data.name}") {
                            withClue("Expected no error, but ${result.error}") {
                                result.error shouldBe null
                            }

                            val expectedEvents = case.events.joinToString("\n") { it.trim() }.trim()
                            val actualEvents = result.events.joinToString("\n") { it.toString().trim() }.trim()

                            actualEvents shouldBe expectedEvents

                            //                    withClue(case.tree) {
                            //                        case.events
                            //                            .zip(result.events)
                            //                            .shouldForAll { (expected, actual) ->
                            //                                expected.trim() shouldBeEqualComparingTo actual.toString().trim()
                            //                            }
                            //                    }
                        }
                    }
                }
            }
        }
    }
})
