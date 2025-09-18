package it.krzeminski.snakeyaml.engine.kmp.issues.issue68

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose

class CommentAfterAliasTest : FunSpec({
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
})
