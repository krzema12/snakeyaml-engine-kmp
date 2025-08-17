package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class NumberCoreTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(CoreSchema()).build())

    test("all integers which are defined in the core schema & JSON") {

    }

    test("all integers which are defined in the core schema but not in JSON") {

    }

    test("all strings which WERE integers or doubles in YAML 1.1") {

    }

    test("all doubles which are defined in the core schema & JSON") {

    }

    test("parse special doubles which are defined in the core schema") {

    }

    test("dump special doubles which are defined in the core schema") {

    }
})
