package it.krzeminski.snakeyaml.engine.kmp.test_suite

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

/**
 * Utilities for running the [YAML Test Suite tests](https://github.com/yaml/yaml-test-suite).
 */
internal object SuiteUtils {

    fun parseData(data: YamlTestData): ParseResult {
        val settings = LoadSettings.builder().setLabel(data.label).build()

        return try {
            ParseResult(Parse(settings).parse(data.inYaml).toList())
        } catch (e: YamlEngineException) {
            ParseResult(null, e)
        }
    }

    /**
     * IDs of cases that should **fail** according to YAML Test Suite, but they actually succeed :(
     *
     * Such deviations are probably because of an enigmatic bug in SnakeYAML-KMP that hasn't been
     * identified and fixed. This is common for YAML libraries, because YAML is far too complicated!
     *
     * As a workaround, the expected outcome of these cases is manually overridden by adding them to
     * this list.
     *
     * If by some chance you've made a change and a case now passes: well done!
     * Remove the case ID from this list.
     *
     * @see deviationsWithError
     */
    val deviationsWithSuccess: Set<YamlTestData.Id> = setOf(
        "9C9N",
        "9JBA",
        "CVW2",
        "QB6E",
        "SU5Z",
        "DK95:01",     // https://matrix.yaml.info/details/DK95:01.html
        "JEF9:02",     // https://matrix.yaml.info/details/JEF9:02.html
        "L24T:01",     // https://matrix.yaml.info/details/L24T:01.html
    ).mapToYamlTestDataId()

    /**
     * IDs of cases that should **pass** according to YAML Test Suite, but they actually fail :(
     *
     * @see deviationsWithSuccess for further explanation.
     */
    val deviationsWithError: Set<YamlTestData.Id> = setOf(
        "3RLN-01",
        "3RLN-04",
        "4MUZ",
        "4MUZ-00",
        "4MUZ-01",
        "4MUZ-02",
        "58MP",
        "5MUD",
        "5T43",
        "6BCT",
        "6HB6",
        "7Z25",
        "9SA2",
        "A2M4",
        "DBG4",
        "DC7X",
        "DE56-02",
        "DE56-03",
        "DK3J",
        "FP8R",
        "FRK4",
        "HM87-00",
        "HWV9",
        "J3BT",
        "K3WX",
        "K54U",
        "KH5V-01",
        "KZN9",
        "M2N8-00",
        "M7A3",
        "MUS6-03",
        "NJ66",
        "Q5MG",
        "QT73",
        "SM9W-01",
        "UKK6-00",
        "UT92",
        "VJP3-01",
        "W4TN",
        "W5VH",
        "WZ62",
        "Y79Y-002",
        "Y79Y-010",
        //region empty-key cases
        // These cases use an empty node as a key. Use of empty keys is discouraged and might be removed
        // in the next YAML version. In short: don't bother trying to fix these tests.
        "2JQS",
        "6M2F",
        "CFD4",
        "FRK4",
        "NHX8",
        "NKF9",
        "S3PD",
        //endregion
        "6CA3",        // https://matrix.yaml.info/details/6CA3.html
        "DK95:00",     // https://matrix.yaml.info/details/DK95:00.html
        "DK95:03",     // https://matrix.yaml.info/details/DK95:03.html
        "DK95:07",     // https://matrix.yaml.info/details/DK95:07.html
    ).mapToYamlTestDataId()

    private fun Set<String>.mapToYamlTestDataId(): Set<YamlTestData.Id> =
        map { id ->
            val updatedId = id.replace("-", ":")
            YamlTestData.Id(updatedId)
        }.toSet()
}
