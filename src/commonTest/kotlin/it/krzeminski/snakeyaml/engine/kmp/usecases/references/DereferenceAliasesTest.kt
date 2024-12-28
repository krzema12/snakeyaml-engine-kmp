package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class DereferenceAliasesTest : FunSpec({
    test("no aliases") {
        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val map = load.loadOne(issue1086_1_input_yaml) as Map<*, *>?
        val setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true).build()
        val dump = Dump(setting)
        val node = dump.dumpToString(map)
        val expected = issue1086_1_expected_yaml
        expected shouldBe node
    }

    test("no aliases recursive") {
        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val map = load.loadOne(issue1086_2_input_yaml) as Map<*, *>?
        val setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDereferenceAliases(true).build()
        val dump = Dump(setting)
        shouldThrow<YamlEngineException> {
            dump.dumpToString(map)
        }.also {
            it.message shouldBe "Cannot dereference aliases for recursive structures."
        }
    }
})

private val issue1086_1_input_yaml = """defines:
  serverPattern1: &server_HighPerformance
    type: t3
    strage: 500
  serverPattern2: &server_LowPerformance
    type: t2
    strage: 250
  lbPattern1: &lb_Public
    name: lbForPublic
    vpc: vpc1
  lbPattern2: &lb_Internal
    name: lbForInternal
    vpc: vpc1
current:
  assenbled1:
    server: *server_HighPerformance
    lb: *lb_Public
"""

private val issue1086_1_expected_yaml = """defines:
  serverPattern1:
    type: t3
    strage: 500
  serverPattern2:
    type: t2
    strage: 250
  lbPattern1:
    name: lbForPublic
    vpc: vpc1
  lbPattern2:
    name: lbForInternal
    vpc: vpc1
current:
  assenbled1:
    server:
      type: t3
      strage: 500
    lb:
      name: lbForPublic
      vpc: vpc1
"""

private val issue1086_2_input_yaml = """&id002
bankAccountOwner: &id001
  bankAccountOwner: *id001
  birthPlace: Leningrad
  birthday: 1970-01-12T13:46:40Z
  children: &id003
    - *id002
    - bankAccountOwner: *id001
      birthPlace: New York
      birthday: 1983-04-24T02:40:00Z
      children: []
      father: *id001
      mother: &id004
        bankAccountOwner: *id001
        birthPlace: Saint-Petersburg
        birthday: 1973-03-03T09:46:40Z
        children: *id003
        father: null
        mother: null
        name: Mother
        partner: *id001
      name: Daughter
      partner: null
  father: null
  mother: null
  name: Father
  partner: *id004
birthPlace: Munich
birthday: 1979-10-28T23:06:40Z
children: []
father: *id001
mother: *id004
name: Son
partner: null
"""
