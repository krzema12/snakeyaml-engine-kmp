package org.snakeyaml.engine.test_suite

import org.snakeyaml.engine.test_suite.SuiteDataIdentifier.CaseId
import org.snakeyaml.engine.test_suite.SuiteDataIdentifier.SuiteId
import kotlin.jvm.JvmInline

internal data class SuiteData(
    val suiteId: SuiteId,
    val name: String,
    val tags: List<String>,
    val cases: List<Case>,
    val dump: String?,
    val tree: String,
) {
    data class Case(
        val caseId: CaseId,
        val yaml: String,
        val tree: String,
        val json: String?,
        val emit: String?,
        val skip: Boolean,
        val fail: Boolean,
    ) {
        val events: List<String> = tree.lines()//.filter { it.isEmpty() }
    }
}

internal sealed interface SuiteDataIdentifier {

    @JvmInline
    value class SuiteId(private val value: String) : SuiteDataIdentifier {
        override fun toString(): String = value
    }

    data class CaseId(
        val suiteId: SuiteId,
        val index: Int,
    ) : SuiteDataIdentifier {
        override fun toString(): String = "$suiteId:${index.toString().padStart(2, '0')}"
    }

    companion object {
        fun Collection<String>.mapToSuiteDataIdentifiers(): List<SuiteDataIdentifier> =
            map { it.replace("-", ":") }
                .map {
                    when {
                        ":" in it ->
                            CaseId(
                                suiteId = SuiteId(it.substringBefore(":")),
                                index = it.substringAfter(":").toInt()
                            )

                        else      -> SuiteId(it)
                    }
                }
    }
}
