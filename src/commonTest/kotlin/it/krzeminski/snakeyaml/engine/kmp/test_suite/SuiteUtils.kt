package it.krzeminski.snakeyaml.engine.kmp.test_suite

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

/**
 * Utilities for running the [YAML Test Suite tests](https://github.com/yaml/yaml-test-suite).
 */
internal object SuiteUtils {

    fun parseData(data: YamlTestData): ParseResult {
        val settings = LoadSettings.builder().setLabel(data.label).build()

        val events = mutableListOf<Event>()
        return runCatching {
            Parse(settings).parse(data.inYaml).forEach(events::add)
            ParseResult(events)
        }.recover {
            ParseResult(events, it)
        }.getOrThrow()
    }

    /**
     * IDs of cases that do **not** have the expected result according to YAML Test Suite :(
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
     * @see deviationsInEvents
     */
    val deviationsInResult: Set<Pair<YamlTestData.Id, String>> = setOf(
        // should fail but pass
        "9C9N" to "Wrong indented flow sequence",
        "9JBA" to "Invalid comment after end of flow sequence",
        "CVW2" to "Invalid comment after comma",
        "QB6E" to "Wrong indented multiline quoted scalar",
        "SU5Z" to "Comment without whitespace after doublequoted scalar",
        "DK95:01" to "Tabs that look like indentation",
        "Y79Y-003" to "Tabs in various contexts",

        // should pass but fail
        "3RLN-01" to "Leading tabs in double quoted",
        "3RLN-04" to "Leading tabs in double quoted",
        "4MUZ-00" to "Flow mapping colon on line after key",
        "4MUZ-01" to "Flow mapping colon on line after key",
        "4MUZ-02" to "Flow mapping colon on line after key",
        "58MP" to "Flow mapping edge cases",
        "5MUD" to "Colon and adjacent value on next line",
        "5T43" to "Colon at the beginning of adjacent flow scalar",
        "6BCT" to "Spec Example 6.3. Separation Spaces",
        "7Z25" to "Bare document after document end marker",
        "9SA2" to "Multiline double quoted flow mapping key",
        "A2M4" to "Spec Example 6.2. Indentation Indicators",
        "DBG4" to "Spec Example 7.10. Plain Characters",
        "DC7X" to "Various trailing tabs",
        "DE56-02" to "Trailing tabs in double quoted",
        "DE56-03" to "Trailing tabs in double quoted",
        "DK3J" to "Zero indented block scalar with line that looks like a comment",
        "FP8R" to "Zero indented block scalar",
        "FRK4" to "Spec Example 7.3. Completely Empty Flow Nodes",
        "HM87-00" to "Scalars in flow start with syntax char",
        "HWV9" to "Document-end marker",
        "J3BT" to "Spec Example 5.12. Tabs and Spaces",
        "K3WX" to "Colon and adjacent value after comment on next line",
        "K54U" to "Tab after document header",
        "KH5V-01" to "Inline tabs in double quoted",
        "M2N8-00" to "Question mark edge cases",
        "M7A3" to "Spec Example 9.3. Bare Documents",
        "MUS6-03" to "Directive variants",
        "NJ66" to "Multiline plain flow mapping key",
        "Q5MG" to "Tab at beginning of line followed by a flow mapping",
        "QT73" to "Comment and document-end marker",
        "SM9W-01" to "Single character streams",
        "UKK6-00" to "Syntax character edge cases",
        "UT92" to "Spec Example 9.4. Explicit Documents",
        "VJP3-01" to "Flow collections over many lines",
        "W4TN" to "Spec Example 9.5. Directives Documents",
        "W5VH" to "Allowed characters in alias",
        "WZ62" to "Spec Example 7.2. Empty Content",
        "Y79Y-010" to "Tabs in various contexts",
        //region empty-key cases
        // These cases use an empty node as a key. Use of empty keys is discouraged and might be removed
        // in the next YAML version. In short: don't bother trying to fix these tests.
        "2JQS" to "Block Mapping with Missing Keys",
        "6M2F" to "Aliases in Explicit Block Mapping",
        "CFD4" to "Empty implicit key in single pair flow sequences",
        "FRK4" to "Spec Example 7.3. Completely Empty Flow Nodes",
        "NHX8" to "Empty Lines at End of Document",
        "NKF9" to "Empty keys in block and flow mapping",
        "S3PD" to "Spec Example 8.18. Implicit Block Mapping Entries",
        //endregion
        "6CA3" to "Tab indented top flow",
        "DK95:00" to "Tabs that look like indentation",
        "DK95:03" to "Tabs that look like indentation",
        "DK95:07" to "Tabs that look like indentation",
    ).mapToYamlTestDataId()

    /**
     * IDs of cases that **do** have the expected result according to YAML Test Suite
     * but did not emit the expected events according to the test suite :(
     *
     * @see deviationsInResult for further explanation.
     */
    val deviationsInEvents: Set<Pair<YamlTestData.Id, String>> = setOf(
        // pass but did emit the wrong events
        "JEF9:02" to "Trailing whitespace in streams",
        "L24T:01" to "Trailing line of spaces",

        // fail but did emit the wrong events
        "2CMS" to "Invalid mapping in plain multiline",
        "4H7K" to "Flow sequence with invalid extra closing bracket",
        "4JVG" to "Scalar value with two anchors",
        "7MNF" to "Missing colon",
        "9CWY" to "Invalid scalar at the end of mapping",
        "9KBC" to "Mapping starting at --- line",
        "CXX2" to "Mapping with anchor on document start line",
        "DK95:06" to "Tabs that look like indentation",
        "EB22" to "Missing document-end marker before directive",
        "EW3V" to "Wrong indendation in mapping",
        "G5U8" to "Plain dashes in flow sequence",
        "H7J7" to "Node anchor not indented",
        "HU3P" to "Invalid Mapping in plain scalar",
        "JKF3" to "Multiline unidented double quoted block key",
        "KS4U" to "Invalid item after end of flow sequence",
        "MUS6:01" to "Directive variants",
        "P2EQ" to "Invalid sequene item on same line as previous item",
        "RHX7" to "YAML directive without document end marker",
        "SR86" to "Anchor plus Alias",
        "SU74" to "Anchor and alias as mapping key",
        "T833" to "Flow mapping missing a separating comma",
        "VJP3:00" to "Flow collections over many lines",
        "Y79Y:000" to "Tabs in various contexts",
        "Y79Y:003" to "Tabs in various contexts",
        "Y79Y:004" to "Tabs in various contexts",
        "Y79Y:005" to "Tabs in various contexts",
        "Y79Y:006" to "Tabs in various contexts",
        "Y79Y:007" to "Tabs in various contexts",
        "Y79Y:008" to "Tabs in various contexts",
        "Y79Y:009" to "Tabs in various contexts",
        "YJV2" to "Dash in flow sequence",
        "ZCZ6" to "Invalid mapping in plain single line value",
    ).mapToYamlTestDataId()

    private fun Set<Pair<String, String>>.mapToYamlTestDataId(): Set<Pair<YamlTestData.Id, String>> =
        map { (id, label) ->
            val updatedId = id
                .replace("-", "_")
                .replace(":", "_")
            YamlTestData.Id(updatedId) to label
        }.toSet()
}
