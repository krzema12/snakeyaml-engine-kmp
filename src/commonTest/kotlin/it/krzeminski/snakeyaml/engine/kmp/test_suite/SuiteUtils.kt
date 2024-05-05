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
            ParseResult(Parse(settings).parseString(data.inYaml).toList())
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
        //region originally copied from SnakeYAML
        // https://github.com/snakeyaml/snakeyaml-engine/blob/5a35e1fe7f780d1405d5a03470f9f13d32b1638a/src/test/java/org/snakeyaml/engine/usecases/external_test_suite/SuiteUtils.java#L33-L34
        "9C9N",
        "9JBA",
        "CVW2",
        "QB6E",
        "SU5Z",
        //endregion

        //region Additional cases that SnakeYAML-Java also doesn't pass
        // (SnakeYAML-Java doesn't use the latest YAML Test Suite data, so it doesn't have
        // manual exclusions for these cases yet).
        "DK95:01",     // https://matrix.yaml.info/details/DK95:01.html
        "JEF9:02",     // https://matrix.yaml.info/details/JEF9:02.html
        "L24T:01",     // https://matrix.yaml.info/details/L24T:01.html
        //endregion
    ).mapToYamlTestDataId()

    /**
     * IDs of cases that should **pass** according to YAML Test Suite, but they actually fail :(
     *
     * @see deviationsWithSuccess for further explanation.
     */
    val deviationsWithError: Set<YamlTestData.Id> = setOf(
        //region originally copied from SnakeYAML
        // https://github.com/snakeyaml/snakeyaml-engine/blob/5a35e1fe7f780d1405d5a03470f9f13d32b1638a/src/test/java/org/snakeyaml/engine/usecases/external_test_suite/SuiteUtils.java#L35-L40
        "2JQS",
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
        "6M2F",
        "7Z25",
        "9SA2",
        "A2M4",
        "CFD4",
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
        "NHX8",
        "NJ66",
        "NKF9",
        "Q5MG",
        "QT73",
        "S3PD",
        "SM9W-01",
        "UKK6-00",
        "UT92",
        "VJP3-01",
        "W4TN",
        "W5VH",
        "WZ62",
        "Y79Y-002",
        "Y79Y-010",
        //endregion

        //region Additional cases that SnakeYAML-Java also doesn't pass
        // (SnakeYAML-Java doesn't use the latest YAML Test Suite data, so it doesn't have
        // manual exclusions for these cases yet).
        "6CA3",        // https://matrix.yaml.info/details/6CA3.html
        "DK95:00",     // https://matrix.yaml.info/details/DK95:00.html
        "DK95:03",     // https://matrix.yaml.info/details/DK95:03.html
        "DK95:07",     // https://matrix.yaml.info/details/DK95:07.html
        //endregion
    ).mapToYamlTestDataId()

    private fun Set<String>.mapToYamlTestDataId(): Set<YamlTestData.Id> =
        map { id ->
            val updatedId = id.replace("-", ":")
            YamlTestData.Id(updatedId)
        }.toSet()
}
