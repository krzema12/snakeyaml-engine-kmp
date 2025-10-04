package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.StringOutputStream
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class DumpAnchorTest : FunSpec({
    test("anchor test") {
        val str = stringFromResources("/anchor/issue481.yaml")
        val compose = Compose(LoadSettings())
        val node = compose.compose(str)!!

        val setting = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setAnchorGenerator { node1: Node -> node1.anchor!! }
            .build()
        val yaml = Dump(setting)

        val writer = StringOutputStream()
        yaml.dumpNode(node, writer)
        writer.toString() shouldBe str
    }
})
