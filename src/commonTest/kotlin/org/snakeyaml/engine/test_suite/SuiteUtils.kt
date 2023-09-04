package org.snakeyaml.engine.test_suite

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.lowlevel.Parse
import org.snakeyaml.engine.v2.exceptions.YamlEngineException

internal object SuiteUtils {

    fun parseData(data: YamlTestData): ParseResult {
        val settings = LoadSettings.builder().setLabel(data.label).build()

        return try {
            ParseResult(Parse(settings).parseString(data.inYaml).toList())
        } catch (e: YamlEngineException) {
            ParseResult(emptyList(), e)
        }
    }

    /**
     * Identifiers of tests that should fail.
     */
    val deviationsWithSuccess: Set<YamlTestData.Id> = setOf(
        //region copied from SnakeYAML
        // https://github.com/snakeyaml/snakeyaml-engine/blob/5a35e1fe7f780d1405d5a03470f9f13d32b1638a/src/test/java/org/snakeyaml/engine/usecases/external_test_suite/SuiteUtils.java#L33-L34
        "9C9N",
        "9JBA",
        "CVW2",
        "QB6E",
        "SU5Z",
        //endregion

        //region Additional cases
        "DK95:01",     // https://matrix.yaml.info/details/DK95:01.html
        "JEF9:02",     // https://matrix.yaml.info/details/JEF9:02.html
        "L24T:01",     // https://matrix.yaml.info/details/L24T:01.html
        //endregion
    ).mapToYamlTestDataId()

    /**
     * Identifiers of tests that fail with an error
     */
    val deviationsWithError: Set<YamlTestData.Id> = setOf(
        //region copied from SnakeYAML
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
        "HS5T",
        "HWV9",
        "J3BT",
        "K3WX",
        "K54U",
        "KH5V-01",
        "KZN9",
        "M2N8-00",
        "M7A3",
        "MUS6-03",
        "NB6Z",
        "NHX8",
        "NJ66",
        "NKF9",
        "Q5MG",
        "QT73",
        "S3PD",
        "SM9W-01",
        "UKK6-00",
        "UT92",
        "UV7Q",
        "VJP3-01",
        "W4TN",
        "W5VH",
        "WZ62",
        "Y79Y-002",
        "Y79Y-010",
        //endregion

        //region Additional cases that SnakeYAML also doesn't pass
        "6CA3",        // https://matrix.yaml.info/details/6CA3.html
        "DK95:00",     // https://matrix.yaml.info/details/DK95:00.html
        "DK95:03",     // https://matrix.yaml.info/details/DK95:03.html
        "DK95:04",     // https://matrix.yaml.info/details/DK95:04.html
        "DK95:05",     // https://matrix.yaml.info/details/DK95:05.html
        "DK95:07",     // https://matrix.yaml.info/details/DK95:07.html
        //endregion
    ).mapToYamlTestDataId()

    private fun Set<String>.mapToYamlTestDataId(): Set<YamlTestData.Id> =
        map { YamlTestData.Id(it.replace("-", ":")) }
            .toSet()
}
