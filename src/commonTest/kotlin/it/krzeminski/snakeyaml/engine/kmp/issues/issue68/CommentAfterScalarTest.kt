package it.krzeminski.snakeyaml.engine.kmp.issues.issue68

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose

class CommentAfterScalarTest : FunSpec({
    val loadSettings = LoadSettings(parseComments = true)

    test("respect inline comment for '!!str # comment'") {
        val compose = Compose(loadSettings)
        val node = compose.compose("!!str # comment")
        node.shouldNotBeNull()
        node.inLineComments?.size shouldBe 1
        node.inLineComments?.first()?.value shouldBe " comment"
    }

    test("respect inline and block comments for '!!str # comment\\n# block comment1'") {
        val compose = Compose(loadSettings)
        val node = compose.compose("!!str # comment\n# block comment1\n# block comment2")
        node.shouldNotBeNull()
        node.inLineComments?.size shouldBe 1
        node.inLineComments?.map { it.value } shouldBe listOf(" comment")
        node.blockComments?.size shouldBe 2
        node.blockComments?.map { it.value } shouldBe listOf(" block comment1", " block comment2")
    }
})
