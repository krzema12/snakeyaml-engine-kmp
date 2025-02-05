package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class JsonScalarResolverTest: FunSpec({
    val scalarResolver = JsonScalarResolver()

    test("resolve explicit scalar") {
        scalarResolver.resolve("1", implicit = false) shouldBe Tag.STR
    }

    test("resolve implicit integer") {
        assertSoftly {
            scalarResolver.resolve("1", implicit = true) shouldBe Tag.INT
            scalarResolver.resolve("112233", implicit = true) shouldBe Tag.INT
            scalarResolver.resolve("-1", implicit = true) shouldBe Tag.INT
            scalarResolver.resolve("+1", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("-01", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("013", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("0", implicit = true) shouldBe Tag.INT
        }
    }

    test("resolve implicit float") {
        assertSoftly {
            scalarResolver.resolve("1.0", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("-1.3", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("+01.445", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("-1.455e45", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("-1.455e-45", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("-1.455E045", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("-1.455E0", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("1.4e4", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("1e4", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("0.0", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve(".inf", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("-.inf", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve(".nan", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("1000.25", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("1.", implicit = true) shouldBe Tag.FLOAT
            scalarResolver.resolve("+1", implicit = true) shouldBe Tag.STR
        }
    }

    test("resolve implicit boolean") {
        assertSoftly {
            scalarResolver.resolve("true", implicit = true) shouldBe Tag.BOOL
            scalarResolver.resolve("false", implicit = true) shouldBe Tag.BOOL
            scalarResolver.resolve("False", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("FALSE", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("off", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("no", implicit = true) shouldBe Tag.STR
        }
    }

    test("resolve implicit null") {
        assertSoftly {
            scalarResolver.resolve("null", implicit = true) shouldBe Tag.NULL
            scalarResolver.resolve("", implicit = true) shouldBe Tag.NULL
        }
    }

    test("resolve implicit strings") {
        assertSoftly {
            scalarResolver.resolve("+.inf", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve(".Inf", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve(".INF", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve(".NAN", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("0xFF", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("True", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("TRUE", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("NULL", implicit = true) shouldBe Tag.STR
            scalarResolver.resolve("~", implicit = true) shouldBe Tag.STR
        }
    }
})
