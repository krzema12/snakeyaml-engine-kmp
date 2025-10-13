package it.krzeminski.snakeyaml.engine.kmp.issues.issue68

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException

class CommentAfterAliasTest : FunSpec({
    listOf(false, true).forEach { parseComments ->
        val loadSettings = LoadSettings(parseComments = parseComments)

        test("inline when parsing comments enabled: $parseComments") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name # inline comment 1
            |  555""".trimMargin()

            if (parseComments) {
                // This test shows a certain undesired behavior that ideally should be fixed one day.
                // See https://github.com/krzema12/snakeyaml-engine-kmp/issues/478.
                shouldThrow<ParserException> {
                    compose.compose(input)
                }.also {
                    it.message shouldContain "while parsing a block mapping"
                    it.message shouldContain "expected <block end>, but found '<scalar>'"
                }
            } else {
                val node = compose.compose(input)
                node.shouldNotBeNull()
            }
        }

        test("block comment and flat after when parsing comments enabled: $parseComments") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    555""".trimMargin()

            if (parseComments) {
                // This test shows a certain undesired behavior that ideally should be fixed one day.
                // See https://github.com/krzema12/snakeyaml-engine-kmp/issues/478.
                shouldThrow<ParserException> {
                    compose.compose(input)
                }.also {
                    it.message shouldContain "while parsing a block mapping"
                    it.message shouldContain "expected <block end>, but found '<scalar>'"
                }
            } else {
                val node = compose.compose(input)
                node.shouldNotBeNull()
            }
        }

        test("block comment and nested after when parsing comments enabled: $parseComments") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    nested_field: nested_value""".trimMargin()

            if (parseComments) {
                // This test shows a certain undesired behavior that ideally should be fixed one day.
                // See https://github.com/krzema12/snakeyaml-engine-kmp/issues/478.
                shouldThrow<ParserException> {
                    compose.compose(input)
                }.also {
                    it.message shouldContain "while parsing a block mapping"
                    it.message shouldContain "expected <block end>, but found '<block mapping start>'"
                }
            } else {
                val node = compose.compose(input)
                node.shouldNotBeNull()
            }
        }
    }
})
