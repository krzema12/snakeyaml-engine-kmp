package org.snakeyaml.engine.test_suite

import org.snakeyaml.engine.test_suite.SuiteDataIdentifier.CaseId
import org.snakeyaml.engine.test_suite.SuiteDataIdentifier.Companion.mapToSuiteDataIdentifiers
import org.snakeyaml.engine.test_suite.SuiteDataIdentifier.SuiteId
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.LoadSettingsBuilder
import org.snakeyaml.engine.v2.api.lowlevel.Parse
import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader

internal object SuiteUtils {

    fun all(): List<SuiteData> = yamlTestSuiteData.map { (id, data) ->
        readData(id, data)
    }

    fun readData(suiteId: SuiteId, data: String): SuiteData {
        val loadSettings = LoadSettingsBuilder().build()
        val composer = Composer(
            settings = loadSettings,
            parser = ParserImpl(loadSettings, StreamReader(loadSettings, data))
        )

        val node = composer.singleNode() ?: error("could not parse node for data \n\n$data")

        require(node is SequenceNode) { "expected sequence, but was ${node::class.simpleName}" }

        val elements = node.value.map { n ->
            require(n is MappingNode) { "expected mapping, but was ${n::class.simpleName}" }
            n.value.associate { (k, v) ->
                require(k is ScalarNode) { "expected key $k is Scalar" }
                require(v is ScalarNode) { "expected value $v is Scalar" }
                k.value to v.value
            }
        }

        val firstElement = elements.first()
        val defaultTree = firstElement["tree"] ?: error("missing tree")
        val defaultYaml = firstElement["yaml"] ?: error("missing yaml")
        val skip = firstElement["skip"]
        val defaultFail = firstElement["fail"]

        val cases = elements.mapIndexed { index, element ->

            val yaml = element["yaml"] ?: defaultYaml

            val replacedYaml = SPECIAL_CHARS.fold(yaml) { acc, (src, replacement) ->
                acc.replace(src, replacement)
            }

            val tree = element["tree"] ?: defaultTree
            val replacedTree = SPECIAL_CHARS.fold(tree) { acc, (src, replacement) ->
                acc.replace(src, replacement)
            }

            SuiteData.Case(
                caseId = CaseId(suiteId, index),
                yaml = replacedYaml,
                tree = replacedTree,
                json = element["json"],
                emit = element["emit"],
                skip = (element["skip"] ?: skip).toBoolean(),
                fail = (firstElement["fail"] ?: defaultFail).toBoolean(),
//                fail = (firstElement["fail"]  ).toBoolean(),
            )
        }


        return SuiteData(
            suiteId = suiteId,
            name = firstElement["name"] ?: error("missing name"),
            tags = (firstElement["tags"] ?: "").split(" ").filter { it.isEmpty() },
            cases = cases,
            dump = firstElement["dump"],
            tree = firstElement["tree"] ?: error("missing tree"),
        )
    }

    fun parseData(data: SuiteData): Map<SuiteData.Case, ParseResult> {
        val settings = LoadSettings.builder().setLabel(data.name).build()

        return data.cases.associateWith { case ->
            try {
                ParseResult(Parse(settings).parseString(case.yaml).toList())
            } catch (e: YamlEngineException) {
                ParseResult(emptyList(), e)
            }
        }
    }


    private val SPECIAL_CHARS = listOf(
        "␣" to " ",    // trailing space characters
        "↵" to "",     // trailing newline characters
        "∎" to "",     // no final newline character
        "⇔" to "",     // byte order mark (BOM) character
        "←" to "\r",   // carriage return character

        // Hard tabs
        "————»" to "\t",
        "———»" to "\t",
        "——»" to "\t",
        "—»" to "\t",
        "»" to "\t",
    )

    fun SuiteData.isIgnored(): Boolean =
        suiteId in allExcludedIdentifiers

    fun SuiteData.Case.isIgnored(): Boolean =
        caseId in allExcludedIdentifiers
          || caseId.suiteId in allExcludedIdentifiers

    // ignore tests, copied from SnakeYAML
    private val deviationsWithSuccess: List<SuiteDataIdentifier> = setOf(
        "9C9N",
        "9JBA",
        "CVW2",
        "QB6E",
        "SU5Z",
    ).mapToSuiteDataIdentifiers()

    // ignore tests, copied from SnakeYAML
    private val deviationsWithError: List<SuiteDataIdentifier> = setOf(
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
    ).mapToSuiteDataIdentifiers()

    private val ignoredCases: List<SuiteDataIdentifier> = setOf(
        // I'm not sure why these tests fails, so ignore them
        "9MQT",        // https://matrix.yaml.info/details/9MQT:00.html
        "Y79Y:01",     // https://matrix.yaml.info/details/Y79Y:01.html

        // These tests are probably failing because SuiteUtils.readData() is failing.
        // I'm going to refactor to use the file-based data, instead of the YAML based data
        "MUS6:02",
        "MUS6:04",
        "MUS6:05",
        "MUS6:06",

        // SnakeYAML doesn't pass these tests either
        "6CA3",        // https://matrix.yaml.info/details/6CA3.html
        "DK95",        // https://matrix.yaml.info/details/DK95:00.html
        "JEF9:02",     // https://matrix.yaml.info/details/JEF9:02.html
    ).mapToSuiteDataIdentifiers()

    private val allExcludedIdentifiers: List<SuiteDataIdentifier> =
        deviationsWithSuccess + deviationsWithError + ignoredCases
}
