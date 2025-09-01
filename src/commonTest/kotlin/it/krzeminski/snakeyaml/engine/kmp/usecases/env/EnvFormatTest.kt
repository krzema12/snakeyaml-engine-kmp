package it.krzeminski.snakeyaml.engine.kmp.usecases.env

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
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
        val envFormat = BaseScalarResolver.ENV_FORMAT

        envFormat.matches($$"${V}") shouldBe true
        envFormat.matches($$"${PATH}") shouldBe true
        envFormat.matches($$"${VARIABLE}") shouldBe true
        envFormat.matches($$"${ VARIABLE}") shouldBe true
        envFormat.matches($$"${ VARIABLE }") shouldBe true
        envFormat.matches($$"${\tVARIABLE  }") shouldBe true

        val matcher = envFormat.matchEntire($$"${VARIABLE}")
        matcher.shouldNotBeNull()
        matcher.groups[1]?.value shouldBe "VARIABLE"
        matcher.groups[2]?.value shouldBe null
        matcher.groups[3]?.value shouldBe null

        envFormat.matches($$"${VARI ABLE}") shouldBe false
    }

    test("match default") {
        val envFormat = BaseScalarResolver.ENV_FORMAT

        envFormat.matches($$"${VARIABLE-default}") shouldBe true
        envFormat.matches($$"${ VARIABLE-default}") shouldBe true
        envFormat.matches($$"${ VARIABLE-default }") shouldBe true
        envFormat.matches($$"${ VARIABLE-}") shouldBe true

        val matcher = envFormat.matchEntire($$"${VARIABLE-default}")
        matcher.shouldNotBeNull()
        matcher.groups[1]?.value shouldBe "VARIABLE"
        matcher.groups[2]?.value shouldBe "-"
        matcher.groups[3]?.value shouldBe "default"

        envFormat.matches($$"${VARIABLE -default}") shouldBe false
        envFormat.matches($$"${VARIABLE - default}") shouldBe false
    }

    test("match default or empty") {
        val envFormat = BaseScalarResolver.ENV_FORMAT

        envFormat.matches($$"${VARIABLE:-default}") shouldBe true
        envFormat.matches($$"${ VARIABLE:-default }") shouldBe true
        envFormat.matches($$"${ VARIABLE:-}") shouldBe true

        val matcher = envFormat.matchEntire($$"${VARIABLE:-default}")
        matcher.shouldNotBeNull()
        matcher.groups[1]?.value shouldBe "VARIABLE"
        matcher.groups[2]?.value shouldBe ":-"
        matcher.groups[3]?.value shouldBe "default"

        envFormat.matches($$"${VARIABLE :-default}") shouldBe false
        envFormat.matches($$"${VARIABLE : -default}") shouldBe false
        envFormat.matches($$"${VARIABLE : - default}") shouldBe false
    }

    test("match error default or empty") {
        val envFormat = BaseScalarResolver.ENV_FORMAT

        envFormat.matches($$"${VARIABLE:?err}") shouldBe true
        envFormat.matches($$"${ VARIABLE:?err }") shouldBe true
        envFormat.matches($$"${ VARIABLE:? }") shouldBe true

        val matcher = envFormat.matchEntire($$"${VARIABLE:?err}")
        matcher.shouldNotBeNull()
        matcher.groups[1]?.value shouldBe "VARIABLE"
        matcher.groups[2]?.value shouldBe ":?"
        matcher.groups[3]?.value shouldBe "err"

        envFormat.matches($$"${ VARIABLE :?err }") shouldBe false
        envFormat.matches($$"${ VARIABLE : ?err }") shouldBe false
        envFormat.matches($$"${ VARIABLE : ? err }") shouldBe false
    }

    test("match error default") {
        val envFormat = BaseScalarResolver.ENV_FORMAT

        envFormat.matches($$"${VARIABLE?err}") shouldBe true
        envFormat.matches($$"${ VARIABLE:?err }") shouldBe true
        envFormat.matches($$"${ VARIABLE:?}") shouldBe true

        val matcher = envFormat.matchEntire($$"${ VARIABLE?err }")
        matcher.shouldNotBeNull()
        matcher.groups[1]?.value shouldBe "VARIABLE"
        matcher.groups[2]?.value shouldBe "?"
        matcher.groups[3]?.value shouldBe "err"

        envFormat.matches($$"${ VARIABLE ?err }") shouldBe false
        envFormat.matches($$"${ VARIABLE ? err }") shouldBe false
    }
})
