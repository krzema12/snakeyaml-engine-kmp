package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.snakeyaml.engine.v2.utils.TestUtils
import java.io.StringWriter

@Tag("fast")
class DereferenceAliasesTest {
    @Test
    fun testNoAliases() {
        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val map = load.loadFromString(TestUtils.getResource("/issues/issue1086-1-input.yaml")) as Map<*, *>?
        val setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true).build()
        val dump = Dump(setting)
        val node = dump.dumpToString(map)
        val out = StringWriter()
        val expected = TestUtils.getResource("/issues/issue1086-1-expected.yaml")
        assertEquals(expected, node)
    }

    @Test
    fun testNoAliasesRecursive() {
        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val map = load.loadFromString(TestUtils.getResource("/issues/issue1086-2-input.yaml")) as Map<*, *>?
        val setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true).build()
        val dump = Dump(setting)
        try {
            dump.dumpToString(map)
            fail()
        } catch (e: YamlEngineException) {
            assertEquals("Cannot dereference aliases for recursive structures.", e.message)
        }
    }
}
