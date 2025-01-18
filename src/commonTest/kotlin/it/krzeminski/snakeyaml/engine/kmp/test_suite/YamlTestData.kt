package it.krzeminski.snakeyaml.engine.kmp.test_suite

import it.krzeminski.snakeyaml.engine.kmp.buildFileSystem
import it.krzeminski.snakeyaml.engine.kmp.internal.FSPath
import it.krzeminski.snakeyaml.engine.kmp.internal.fsPath
import okio.buffer
import kotlin.jvm.JvmInline


/**
 * Test data sourced from https://github.com/yaml/yaml-test-suite
 */
sealed interface YamlTestData {
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

val YamlTestResourcesFileSystem = buildFileSystem(YamlTestSuiteResources.resourcesMap)

val YamlTestSuiteData: Map<YamlTestData.Id, YamlTestData> =
    YamlTestResourcesFileSystem.fsPath("/").listRecursively()
        .filter { it.isDirectory && it.resolve("===").exists }
        .associate { dir ->
            val id = YamlTestData.Id(dir.path.segments.joinToString("_"))
            id to generateYamlTestDataObject(id, dir)
        }

private fun generateYamlTestDataObject(
    id: YamlTestData.Id,
    path: FSPath,
): YamlTestData {
    val isError = path.resolve("error").exists
    return if (isError) {
        object : YamlTestData.Error {
            override val id: YamlTestData.Id = id
            override val label: String = path.resolve("===").source().buffer().readUtf8()
            override val inYaml: String = path.resolve("in.yaml").source().buffer().readUtf8()

        }
    } else {
        object : YamlTestData.Success {
            override val id: YamlTestData.Id = id
            override val testEvent: String = path.resolve("test.event").source().buffer().readUtf8()
            override val label: String = path.resolve("===").source().buffer().readUtf8()
            override val inYaml: String = path.resolve("in.yaml").source().buffer().readUtf8()
            override val outYaml: String? = path.resolve("out.yaml").let {
                if (it.exists) it.source().buffer().readUtf8() else null
            }
            override val emitYaml: String? = path.resolve("emit.yaml").let {
                if (it.exists) it.source().buffer().readUtf8() else null
            }
            override val inJson: String? = path.resolve("in.json").let {
                if (it.exists) it.source().buffer().readUtf8() else null
            }
        }
    }
}
