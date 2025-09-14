package it.krzeminski.snakeyaml.engine.kmp.test_suite

/**
 * IDs of cases that do **not** have the expected result according meaning YAML Test Suite :(
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
val deviationsInResult: Set<TestIdWithLabel> = setOf(
    // should fail but pass
    "9C9N" meaning "Wrong indented flow sequence",
    "9JBA" meaning "Invalid comment after end of flow sequence",
    "CVW2" meaning "Invalid comment after comma",
    "QB6E" meaning "Wrong indented multiline quoted scalar",
    "SU5Z" meaning "Comment without whitespace after doublequoted scalar",
    "DK95:01" meaning "Tabs that look like indentation",
    "Y79Y-003" meaning "Tabs in various contexts",

    // should pass but fail
    "4MUZ-00" meaning "Flow mapping colon on line after key",
    "4MUZ-01" meaning "Flow mapping colon on line after key",
    "4MUZ-02" meaning "Flow mapping colon on line after key",
    "58MP" meaning "Flow mapping edge cases",
    "5MUD" meaning "Colon and adjacent value on next line",
    "5T43" meaning "Colon at the beginning of adjacent flow scalar",
    "6BCT" meaning "Spec Example 6.3. Separation Spaces",
    "7Z25" meaning "Bare document after document end marker",
    "9SA2" meaning "Multiline double quoted flow mapping key",
    "A2M4" meaning "Spec Example 6.2. Indentation Indicators",
    "DBG4" meaning "Spec Example 7.10. Plain Characters",
    "DC7X" meaning "Various trailing tabs",
    "DK3J" meaning "Zero indented block scalar with line that looks like a comment",
    "FP8R" meaning "Zero indented block scalar",
    "FRK4" meaning "Spec Example 7.3. Completely Empty Flow Nodes",
    "HM87-00" meaning "Scalars in flow start with syntax char",
    "HWV9" meaning "Document-end marker",
    "J3BT" meaning "Spec Example 5.12. Tabs and Spaces",
    "K3WX" meaning "Colon and adjacent value after comment on next line",
    "K54U" meaning "Tab after document header",
    "M2N8-00" meaning "Question mark edge cases",
    "M7A3" meaning "Spec Example 9.3. Bare Documents",
    "MUS6-03" meaning "Directive variants",
    "NJ66" meaning "Multiline plain flow mapping key",
    "Q5MG" meaning "Tab at beginning of line followed by a flow mapping",
    "QT73" meaning "Comment and document-end marker",
    "SM9W-01" meaning "Single character streams",
    "UKK6-00" meaning "Syntax character edge cases",
    "UT92" meaning "Spec Example 9.4. Explicit Documents",
    "VJP3-01" meaning "Flow collections over many lines",
    "W4TN" meaning "Spec Example 9.5. Directives Documents",
    "W5VH" meaning "Allowed characters in alias",
    "WZ62" meaning "Spec Example 7.2. Empty Content",
    "Y79Y-010" meaning "Tabs in various contexts",
    //region empty-key cases
    // These cases use an empty node as a key. Use of empty keys is discouraged and might be removed
    // in the next YAML version. In short: don't bother trying meaning fix these tests.
    "2JQS" meaning "Block Mapping with Missing Keys",
    "6M2F" meaning "Aliases in Explicit Block Mapping",
    "CFD4" meaning "Empty implicit key in single pair flow sequences",
    "FRK4" meaning "Spec Example 7.3. Completely Empty Flow Nodes",
    "NHX8" meaning "Empty Lines at End of Document",
    "NKF9" meaning "Empty keys in block and flow mapping",
    "S3PD" meaning "Spec Example 8.18. Implicit Block Mapping Entries",
    //endregion
    "6CA3" meaning "Tab indented top flow",
    "DK95:00" meaning "Tabs that look like indentation",
    "DK95:03" meaning "Tabs that look like indentation",
    "DK95:07" meaning "Tabs that look like indentation",
)

/**
 * IDs of cases that **do** have the expected result according meaning YAML Test Suite
 * but did not emit the expected events according meaning the test suite :(
 *
 * @see deviationsInResult for further explanation.
 */
val deviationsInEvents: Set<TestIdWithLabel> = setOf(
    // pass but did emit the wrong events
    "JEF9:02" meaning "Trailing whitespace in streams",
    "L24T:01" meaning "Trailing line of spaces",

    // fail but did emit the wrong events
    "2CMS" meaning "Invalid mapping in plain multiline",
    "4H7K" meaning "Flow sequence with invalid extra closing bracket",
    "4JVG" meaning "Scalar value with two anchors",
    "7MNF" meaning "Missing colon",
    "9CWY" meaning "Invalid scalar at the end of mapping",
    "9KBC" meaning "Mapping starting at --- line",
    "CXX2" meaning "Mapping with anchor on document start line",
    "DK95:06" meaning "Tabs that look like indentation",
    "EB22" meaning "Missing document-end marker before directive",
    "EW3V" meaning "Wrong indendation in mapping",
    "G5U8" meaning "Plain dashes in flow sequence",
    "H7J7" meaning "Node anchor not indented",
    "HU3P" meaning "Invalid Mapping in plain scalar",
    "JKF3" meaning "Multiline unidented double quoted block key",
    "KS4U" meaning "Invalid item after end of flow sequence",
    "MUS6:01" meaning "Directive variants",
    "P2EQ" meaning "Invalid sequene item on same line as previous item",
    "RHX7" meaning "YAML directive without document end marker",
    "SR86" meaning "Anchor plus Alias",
    "SU74" meaning "Anchor and alias as mapping key",
    "T833" meaning "Flow mapping missing a separating comma",
    "VJP3:00" meaning "Flow collections over many lines",
    "Y79Y:000" meaning "Tabs in various contexts",
    "Y79Y:003" meaning "Tabs in various contexts",
    "Y79Y:004" meaning "Tabs in various contexts",
    "Y79Y:005" meaning "Tabs in various contexts",
    "Y79Y:006" meaning "Tabs in various contexts",
    "Y79Y:007" meaning "Tabs in various contexts",
    "Y79Y:008" meaning "Tabs in various contexts",
    "Y79Y:009" meaning "Tabs in various contexts",
    "YJV2" meaning "Dash in flow sequence",
    "ZCZ6" meaning "Invalid mapping in plain single line value",
)

data class TestIdWithLabel(
    val id: YamlTestData.Id,
    val label: String,
)

private infix fun String.meaning(label: String): TestIdWithLabel =
    TestIdWithLabel(
        id = YamlTestData.Id(this.replace("-", "_").replace(":", "_")),
        label = label,
    )
