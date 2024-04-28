package it.krzeminski.snakeyaml.engine.kmp.test_suite

import kotlin.jvm.JvmInline


/**
 * Test data sourced from https://github.com/yaml/yaml-test-suite
 */
internal sealed interface YamlTestData {
    val id: Id
    val label: String
    val inYaml: String
    val outYaml: String?
    val emitYaml: String?
    val inJson: String?
    val testEvent: String?

    @JvmInline
    value class Id(private val value: String) {
        override fun toString(): String = value
    }

    interface Error : YamlTestData {
        override val outYaml: String? get() = null
        override val emitYaml: String? get() = null
        override val inJson: String? get() = null
        override val testEvent: String? get() = null
    }

    interface Success : YamlTestData {
        override val testEvent: String
    }
}
