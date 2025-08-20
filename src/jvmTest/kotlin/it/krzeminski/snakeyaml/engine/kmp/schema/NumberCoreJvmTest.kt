package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import java.math.BigInteger

// TODO implement BigInteger for platforms other than the JVM in https://github.com/krzema12/snakeyaml-engine-kmp/issues/49
//  For now, this test just covers long number on the JVM where we have BigInteger.
class NumberCoreJvmTest : FunSpec({
    test("integer too long to fit in an integer or a long") {
        val loader = Load(LoadSettings.builder().setSchema(CoreSchema()).build())
        loader.loadOne("1234567890123456789123") shouldBe BigInteger("1234567890123456789123")
    }
})
