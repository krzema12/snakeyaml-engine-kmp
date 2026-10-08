package it.krzeminski.snakeyaml.engine.kmp.issues.issue79

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructScalar
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonFloat
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources
import java.math.BigDecimal

/**
 * Ported from issue 79 in SnakeYAML Engine.
 *
 * Float values should preserve decimal precision using BigDecimal.
 *
 * Adaptation: upstream subclasses `ConstructYamlJsonFloat` and overrides its protected
 * `constructFromString`. In KMP that method is private (and returns `Double`), so the
 * constructor here extends [ConstructScalar] and delegates special values to [ConstructYamlJsonFloat].
 * BigDecimal is JVM-only, hence this test lives in jvmTest.
 */
class FloatPrecisionTest : FunSpec({
    class ConstructBigDecimalFloat : ConstructScalar() {
        private val delegate = ConstructYamlJsonFloat()

        override fun construct(node: Node?): Any {
            val value = constructScalar(node)
            return if (value in setOf(".inf", "-.inf", ".nan")) delegate.construct(node) else BigDecimal(value)
        }
    }

    test("float values should be parsed as BigDecimal for exact decimal precision") {
        val yaml = stringFromResources("/issues/issue79-input.yaml")
        val settings = LoadSettings(tagConstructors = mapOf(Tag.FLOAT to ConstructBigDecimalFloat()))
        val root = Load(settings).loadOne(yaml) as Map<*, *>

        fun Any?.map() = this as Map<*, *>
        val examples = root["responses"].map()["200"].map()["content"].map()["application/json"].map()["examples"].map()

        val simpleAmount = examples["simple"].map()["value"].map()["amount"]
        val multimodalAmount = examples["multimodal"].map()["value"].map()["amount"]

        simpleAmount.shouldBeInstanceOf<BigDecimal>()
        simpleAmount shouldBe BigDecimal("1.202")
        multimodalAmount.shouldBeInstanceOf<BigDecimal>()
        multimodalAmount shouldBe BigDecimal("0.00341775")
    }
})
