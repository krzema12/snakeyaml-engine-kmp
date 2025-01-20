package it.krzeminski.snakeyaml.engine.kmp.test_suite

import it.krzeminski.snakeyaml.engine.kmp.buildFileSystem
import it.krzeminski.snakeyaml.engine.kmp.internal.FSPath
import it.krzeminski.snakeyaml.engine.kmp.internal.fsPath
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

val YamlTestResourcesFileSystem = buildFileSystem(YamlTestSuiteResources.resourcesMap)

internal val YamlTestSuiteData: Map<YamlTestData.Id, YamlTestData> =
    YamlTestResourcesFileSystem.fsPath("/").listRecursively()
        .filter { it.isDirectory && (it / "===").exists }
        .associate { dir ->
            val id = YamlTestData.Id(dir.path.segments.joinToString(":"))
            id to generateYamlTestDataObject(id, dir)
        }

private fun generateYamlTestDataObject(
    id: YamlTestData.Id,
    path: FSPath,
): YamlTestData {
    val isError = (path / "error").exists
    return if (isError) {
        object : YamlTestData.Error {
            override val id: YamlTestData.Id = id
            override val label: String = (path / "===").readUtf8()
            override val inYaml: String = (path / "in.yaml").readUtf8()

        }
    } else {
        object : YamlTestData.Success {
            override val id: YamlTestData.Id = id
            override val label: String = (path / "===").readUtf8()
            override val inYaml: String = (path / "in.yaml").readUtf8()
            override val testEvent: String = (path / "test.event").readUtf8()
            override val outYaml: String? = (path / "out.yaml").readUtf8OrNull()
            override val emitYaml: String? = (path / "emit.yaml").readUtf8OrNull()
            override val inJson: String? = (path / "in.json").readUtf8OrNull()
        }
    }
}
