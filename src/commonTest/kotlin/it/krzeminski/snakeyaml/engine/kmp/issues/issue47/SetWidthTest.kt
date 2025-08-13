package it.krzeminski.snakeyaml.engine.kmp.issues.issue47

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StringStreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.*

class SetWidthTest : FunSpec({
    val stringToSerialize = "arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/\${{ github.token }}"

    test("emitted plain text via Dump with limited width should be split") {
        val settings = DumpSettings.builder().setWidth(80).build() // Intentionally limited.
        val writer = StringStreamDataWriter()
        val dump = Dump(settings)
        dump.dump(stringToSerialize, writer)
        val yaml = writer.toString()
        val expected = "arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/\${{\n  github.token }}"
        parseBack(yaml) shouldBe stringToSerialize
        yaml.trim() shouldBe expected
    }

    test("emitted plain text via Emitter with limited width should be split") {
        val settings = DumpSettings.builder().setWidth(80).build() // Intentionally limited.
        val writer = StringStreamDataWriter()
        with(Emitter(settings, writer)) {
            emit(StreamStartEvent())
            emit(DocumentStartEvent(explicit = false, specVersion = null, tags = emptyMap()))
            emit(ScalarEvent(
                anchor = null,
                tag = null,
                implicit = ImplicitTuple(plain = true, nonPlain = true),
                value = stringToSerialize,
                scalarStyle = ScalarStyle.PLAIN,
            ))
            emit(DocumentEndEvent(isExplicit = false))
            emit(StreamEndEvent())
        }
        val yaml = writer.toString()
        val expected = "arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/\${{\n  github.token }}\n"
        yaml shouldBe expected
    }

    test("emitted plain text via Emitter with width longer than emitted text should not be split") {
        val settings = DumpSettings.builder().setWidth(180).build() // Intentionally limited.
        val writer = StringStreamDataWriter()
        with(Emitter(settings, writer)) {
            emit(StreamStartEvent())
            emit(DocumentStartEvent(explicit = false, specVersion = null, tags = emptyMap()))
            emit(ScalarEvent(
                anchor = null,
                tag = null,
                implicit = ImplicitTuple(plain = true, nonPlain = true),
                value = stringToSerialize,
                scalarStyle = ScalarStyle.PLAIN,
            ))
            emit(DocumentEndEvent(isExplicit = false))
            emit(StreamEndEvent())
        }
        val yaml = writer.toString()
        val expected = "arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/\${{ github.token }}\n"
        yaml shouldBe expected
        parseBack(yaml) shouldBe stringToSerialize
    }

    test("emitted plain text with limited width and folded scalar style should be split") {
        val settings = DumpSettings.builder()
            .setWidth(80) // Intentionally limited.
            .setDefaultScalarStyle(ScalarStyle.FOLDED)
            .build()
        val dump = Dump(settings)
        val yaml = dump.dumpToString(stringToSerialize)
        val expected = ">-\n" +
            "  arn:aws:iam::12345678901234567890:foobarbaz:testing:testing2:role/github-actions-role/\${{\n" +
            "  github.token }}\n"
        yaml shouldBe expected
        parseBack(yaml) shouldBe stringToSerialize
    }
})

private fun parseBack(yaml: String): String {
    val settings = LoadSettings.builder().build()
    val load = Load(settings)
    return load.loadOne(yaml).toString()
}
