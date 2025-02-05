package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.regex.shouldMatch
import io.kotest.matchers.regex.shouldNotMatch
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class JsonScalarResolverTest : FunSpec({
    val scalarResolver = JsonScalarResolver()

    test("Resolve explicit scalar") {
        scalarResolver.resolve("1", false) shouldBe Tag.STR
    }

    test("Resolve implicit integer") {
        scalarResolver.resolve("1", true) shouldBe Tag.INT
        scalarResolver.resolve("112233", true) shouldBe Tag.INT
        scalarResolver.resolve("-1", true) shouldBe Tag.INT
        scalarResolver.resolve("+1", true) shouldBe Tag.STR
        scalarResolver.resolve("-01", true) shouldBe Tag.STR
        scalarResolver.resolve("013", true) shouldBe Tag.STR
        scalarResolver.resolve("0", true) shouldBe Tag.INT
    }

    test("Resolve implicit float") {
        scalarResolver.resolve("1.0", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("-1.3", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("+01.445", true) shouldBe Tag.STR
        scalarResolver.resolve("-1.455e45", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("-1.455e-45", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("-1.455E045", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("-1.455E0", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("1.4e4", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("1e4", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("0.0", true) shouldBe Tag.FLOAT
        scalarResolver.resolve(".inf", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("-.inf", true) shouldBe Tag.FLOAT
        scalarResolver.resolve(".nan", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("1000.25", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("1.", true) shouldBe Tag.FLOAT
        scalarResolver.resolve("+1", true) shouldBe Tag.STR
    }

    test("Resolve implicit boolean") {
        scalarResolver.resolve("true", true) shouldBe Tag.BOOL
        scalarResolver.resolve("false", true) shouldBe Tag.BOOL
        scalarResolver.resolve("False", true) shouldBe Tag.STR
        scalarResolver.resolve("FALSE", true) shouldBe Tag.STR
        scalarResolver.resolve("off", true) shouldBe Tag.STR
        scalarResolver.resolve("no", true) shouldBe Tag.STR
    }

    test("Resolve implicit null") {
        scalarResolver.resolve("null", true) shouldBe Tag.NULL
        scalarResolver.resolve("", true) shouldBe Tag.NULL
    }

    test("Resolve implicit strings") {
        scalarResolver.resolve("+.inf", true) shouldBe Tag.STR
        scalarResolver.resolve(".Inf", true) shouldBe Tag.STR
        scalarResolver.resolve(".INF", true) shouldBe Tag.STR
        scalarResolver.resolve(".NAN", true) shouldBe Tag.STR
        scalarResolver.resolve("0xFF", true) shouldBe Tag.STR
        scalarResolver.resolve("True", true) shouldBe Tag.STR
        scalarResolver.resolve("TRUE", true) shouldBe Tag.STR
        scalarResolver.resolve("NULL", true) shouldBe Tag.STR
        scalarResolver.resolve("~", true) shouldBe Tag.STR
    }
})
