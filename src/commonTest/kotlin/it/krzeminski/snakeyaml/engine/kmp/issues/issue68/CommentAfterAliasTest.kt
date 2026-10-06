package it.krzeminski.snakeyaml.engine.kmp.issues.issue68

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose

/**
 * Issue 68: Comments are not parsed correctly when they follow an alias
 */
class CommentAfterAliasTest : FunSpec({
    listOf(false, true).forEach { parseComments ->
        val loadSettings = LoadSettings(parseComments = parseComments)

        test("Issue 68: inline when parsing comments enabled: $parseComments") {
            val input = """
            |field_with_alias: &alias_name # inline comment 1
            |  555""".trimMargin()

            Compose(loadSettings).compose(input).shouldNotBeNull()
            Load(loadSettings).loadOne(input) shouldBe mapOf("field_with_alias" to 555)
        }

        test("Issue 68: block comment and flat after when parsing comments enabled: $parseComments") {
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    555""".trimMargin()

            Compose(loadSettings).compose(input).shouldNotBeNull()
            Load(loadSettings).loadOne(input) shouldBe mapOf("field_with_alias" to 555)
        }

        test("Issue 68: block comment and nested after when parsing comments enabled: $parseComments") {
            val input = """
            |field_with_alias: &alias_name
            |# separate line comment following the alias
            |    nested_field: nested_value""".trimMargin()

            Compose(loadSettings).compose(input).shouldNotBeNull()
            Load(loadSettings).loadOne(input) shouldBe
                mapOf("field_with_alias" to mapOf("nested_field" to "nested_value"))
        }

        test("Issue 68: tag with inline comment when parsing comments enabled: $parseComments") {
            val input = "key: !!str # comment\n  value"

            Compose(loadSettings).compose(input).shouldNotBeNull()
            Load(loadSettings).loadOne(input) shouldBe mapOf("key" to "value")
        }

        test("Issue 68: anchor and tag with comment when parsing comments enabled: $parseComments") {
            val input = "key: &anchor !!str # comment\n  value"

            Compose(loadSettings).compose(input).shouldNotBeNull()
            Load(loadSettings).loadOne(input) shouldBe mapOf("key" to "value")
        }

        test("Issue 68: tag with comment and no content as a nested value when parsing comments enabled: $parseComments") {
            val input = "a: !!str # c\nb: 1\n"

            Load(loadSettings).loadOne(input) shouldBe mapOf("a" to "", "b" to 1)
        }

        test("Issue 68: tag with comment and no content as the document root when parsing comments enabled: $parseComments") {
            val input = "!!str # c\n"

            Load(loadSettings).loadOne(input) shouldBe ""
        }
    }

    val commentSettings = LoadSettings(parseComments = true)

    test("Issue 68: comment between anchor and flow sequence") {
        Load(commentSettings).loadOne("k: &a # c\n  [1, 2]\n") shouldBe mapOf("k" to listOf(1, 2))
    }

    test("Issue 68: comment between tag and indentless block sequence") {
        Load(commentSettings).loadOne("k: !!seq # c\n- 1\n") shouldBe mapOf("k" to listOf(1))
    }

    test("Issue 68: two comments between anchor and scalar") {
        Load(commentSettings).loadOne("k: &a # c1\n  # c2\n  v\n") shouldBe mapOf("k" to "v")
    }

    test("Issue 68: comment between tag and flow mapping") {
        Load(commentSettings).loadOne("k: !!map # c\n  {x: 1}\n") shouldBe mapOf("k" to mapOf("x" to 1))
    }
})
