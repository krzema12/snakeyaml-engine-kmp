package it.krzeminski.snakeyaml.engine.kmp.usecases.env

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.resolver.BaseScalarResolver

/**
 * {@code ${VARIABLE:-default}} evaluates to default if {@code VARIABLE} is unset or empty in the environment.
 * {@code ${VARIABLE-default}} evaluates to default only if VARIABLE is unset in the environment.
 *
 * Similarly, the following syntax allows you to specify mandatory variables:
 *
 * {@code ${VARIABLE:?err}} exits with an error message containing err if {@code VARIABLE} is unset or empty in the
 * environment. {@code ${VARIABLE?err}} exits with an error message containing err if {@code VARIABLE} is unset in
 * the environment.
 */
class EnvFormatTest : FunSpec({
    test("match basic") {
        val envFormat = BaseScalarResolver.ENV_FORMAT.toPattern()

        envFormat.matcher("\${V}").matches() shouldBe true
        envFormat.matcher("\${PATH}").matches() shouldBe true
        envFormat.matcher("\${VARIABLE}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE }").matches() shouldBe true
        envFormat.matcher("\${\tVARIABLE  }").matches() shouldBe true

        val matcher = envFormat.matcher("\${VARIABLE}")
        matcher.matches() shouldBe true
        matcher.group(1) shouldBe "VARIABLE"
        matcher.group(2) shouldBe null
        matcher.group(3) shouldBe null

        envFormat.matcher("\${VARI ABLE}").matches() shouldBe false
    }

    test("match default") {
        val envFormat = BaseScalarResolver.ENV_FORMAT.toPattern()

        envFormat.matcher("\${VARIABLE-default}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE-default}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE-default }").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE-}").matches() shouldBe true

        val matcher = envFormat.matcher("\${VARIABLE-default}")
        matcher.matches() shouldBe true
        matcher.group(1) shouldBe "VARIABLE"
        matcher.group(2) shouldBe "-"
        matcher.group(3) shouldBe "default"

        envFormat.matcher("\${VARIABLE -default}").matches() shouldBe false
        envFormat.matcher("\${VARIABLE - default}").matches() shouldBe false
    }

    test("match default or empty") {
        val envFormat = BaseScalarResolver.ENV_FORMAT.toPattern()

        envFormat.matcher("\${VARIABLE:-default}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE:-default }").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE:-}").matches() shouldBe true

        val matcher = envFormat.matcher("\${VARIABLE:-default}")
        matcher.matches() shouldBe true
        matcher.group(1) shouldBe "VARIABLE"
        matcher.group(2) shouldBe ":-"
        matcher.group(3) shouldBe "default"

        envFormat.matcher("\${VARIABLE :-default}").matches() shouldBe false
        envFormat.matcher("\${VARIABLE : -default}").matches() shouldBe false
        envFormat.matcher("\${VARIABLE : - default}").matches() shouldBe false
    }

    test("match error default or empty") {
        val envFormat = BaseScalarResolver.ENV_FORMAT.toPattern()

        envFormat.matcher("\${VARIABLE:?err}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE:?err }").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE:? }").matches() shouldBe true

        val matcher = envFormat.matcher("\${VARIABLE:?err}")
        matcher.matches() shouldBe true
        matcher.group(1) shouldBe "VARIABLE"
        matcher.group(2) shouldBe ":?"
        matcher.group(3) shouldBe "err"

        envFormat.matcher("\${ VARIABLE :?err }").matches() shouldBe false
        envFormat.matcher("\${ VARIABLE : ?err }").matches() shouldBe false
        envFormat.matcher("\${ VARIABLE : ? err }").matches() shouldBe false
    }

    test("match error default") {
        val envFormat = BaseScalarResolver.ENV_FORMAT.toPattern()

        envFormat.matcher("\${VARIABLE?err}").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE:?err }").matches() shouldBe true
        envFormat.matcher("\${ VARIABLE:?}").matches() shouldBe true

        val matcher = envFormat.matcher("\${ VARIABLE?err }")
        matcher.matches() shouldBe true
        matcher.group(1) shouldBe "VARIABLE"
        matcher.group(2) shouldBe "?"
        matcher.group(3) shouldBe "err"

        envFormat.matcher("\${ VARIABLE ?err }").matches() shouldBe false
        envFormat.matcher("\${ VARIABLE ? err }").matches() shouldBe false
    }
})
