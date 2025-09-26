package it.krzeminski.snakeyaml.engine.kmp.issues.issue68

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException

class CommentAfterAliasTest : FunSpec({
    context("parsing comments is disabled") {
        val loadSettings = LoadSettings.builder().setParseComments(false).build()

        test("inline") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name # inline comment 1
            |  555""".trimMargin()
            val node = compose.compose(input)
            node.shouldNotBeNull()
        }

        test("block comment and flat after") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    555""".trimMargin()
            val node = compose.compose(input)
            node.shouldNotBeNull()
        }

        test("block comment and nested after") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    nested_field: nested_value""".trimMargin()
            val node = compose.compose(input)
            node.shouldNotBeNull()
        }
    }

    context("parsing comments is enabled") {
        val loadSettings = LoadSettings.builder().setParseComments(true).build()

        test("inline") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name # inline comment 1
            |  555""".trimMargin()
            // This test shows a certain undesired behavior that ideally should be fixed one day.
            // See https://github.com/krzema12/snakeyaml-engine-kmp/issues/478.
            shouldThrow<ParserException> {
                compose.compose(input)
            }.also {
                it.message shouldContain "while parsing a block mapping"
                it.message shouldContain "expected <block end>, but found '<scalar>'"
            }
        }

        test("block comment and flat after") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    555""".trimMargin()
            // This test shows a certain undesired behavior that ideally should be fixed one day.
            // See https://github.com/krzema12/snakeyaml-engine-kmp/issues/478.
            shouldThrow<ParserException> {
                compose.compose(input)
            }.also {
                it.message shouldContain "while parsing a block mapping"
                it.message shouldContain "expected <block end>, but found '<scalar>'"
            }
        }

        test("block comment and nested after") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    nested_field: nested_value""".trimMargin()
            // This test shows a certain undesired behavior that ideally should be fixed one day.
            // See https://github.com/krzema12/snakeyaml-engine-kmp/issues/478.
            shouldThrow<ParserException> {
                compose.compose(input)
            }.also {
                it.message shouldContain "while parsing a block mapping"
                it.message shouldContain "expected <block end>, but found '<block mapping start>'"
            }
        }
    }
})
