package it.krzeminski.snakeyaml.engine.kmp.issues.issue68

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose

class CommentAfterAliasTest : FunSpec({
    listOf(false, true).forEach { parseComments ->
        val loadSettings = LoadSettings(parseComments = parseComments)

        test("inline when parsing comments enabled: $parseComments") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name # inline comment 1
            |  555""".trimMargin()
            compose.compose(input).shouldNotBeNull()
        }

        test("block comment and flat after when parsing comments enabled: $parseComments") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    555""".trimMargin()
            compose.compose(input).shouldNotBeNull()
        }

        test("block comment and nested after when parsing comments enabled: $parseComments") {
            val compose = Compose(loadSettings)
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    nested_field: nested_value""".trimMargin()
            compose.compose(input).shouldNotBeNull()
        }
    }
})
