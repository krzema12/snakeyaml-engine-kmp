package buildsrc.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.intellij.lang.annotations.Subst
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class GenerateYamlTestSuiteData @Inject constructor(
    private val fs: FileSystemOperations
) : DefaultTask() {

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(RELATIVE)
    abstract val yamlTestSuiteFilesDir: DirectoryProperty

    private val rootDir = project.rootDir

    @TaskAction
    fun action() {
        val destination = destination.asFile.get()
        fs.delete { delete(destination) }
        destination.mkdirs()

        val yamlTestSuiteDirs = yamlTestSuiteFilesDir.get().asFile
            .walk()
            .filter { it.isDirectory && it.resolve("===").exists() }

        data class YamlTestSuiteDirSpec(
            val id: String,
            val dir: File,
            val objectName: String = "DATA_${id.replace(":", "_")}",
        )

        val yamlTestSuiteDirsById = yamlTestSuiteDirs.map { dir ->
            val parentDirContents = dir.parentFile.listFiles()?.toList() ?: emptyList()
            val id = if (parentDirContents.all { it.isDirectory && it.name.toIntOrNull() != null }) {
                "${dir.parentFile.name}:${dir.name}"
            } else {
                dir.name
            }
            YamlTestSuiteDirSpec(id = id, dir = dir)
        }

        yamlTestSuiteDirsById.forEach { (id, testCaseDir, objectName) ->
            // error -- This file indicates the YAML should fail to parse
            val expectError = testCaseDir.resolve("error").exists()

            val contents = if (expectError) {
                generateExpectErrorData(
                    objectName = objectName,
                    id = id,
                    testCaseDir = testCaseDir,
                )
            } else {
                generateExpectSuccessData(
                    objectName = objectName,
                    id = id,
                    testCaseDir = testCaseDir,
                )
            }

            val destFile = destination.resolve("$objectName.kt")

            destFile.writeText(contents)
        }

        val allTestSuiteIdListContents = yamlTestSuiteDirsById
            .map { (_, _, objectName) -> "$objectName.id to $objectName," }
            .sorted()
            .joinToString("\n")
            .prependIndent("  ")

        val yamlTestSuiteValuesFile = temporaryDir.resolve("yamlTestSuiteValues.kt")

        // use `internal object` instead of `internal val` to try and avoid https://youtrack.jetbrains.com/issue/KT-59274
        yamlTestSuiteValuesFile.writeText(/* language=kotlin */ """
            ¦package org.snakeyaml.engine.test_suite
            ¦
            ¦/** All YAML test suite data */
            ¦internal object YamlTestSuiteData : Map<YamlTestData.Id, YamlTestData> by mapOf(
            ¦$allTestSuiteIdListContents
            ¦)
            ¦
        """.trimMargin(/* language=text */ "¦")
        )
    }

    private fun generateExpectErrorData(
        objectName: String,
        id: String,
        testCaseDir: File,
    ): String {
        // === -- The name/label of the test
        val label = testCaseDir.resolve("===").takeIf { it.exists() }?.readText()
            ?: error("missing === label in $testCaseDir")
        // in.yaml -- The YAML input to be parsed or loaded
        val inYaml = testCaseDir.resolve("in.yaml").takeIf { it.exists() }?.readText()
            ?: error("missing in.yaml in $testCaseDir")

        val relativePath = testCaseDir.relativeTo(rootDir).invariantSeparatorsPath

        return /* language=kotlin */ """
            ¦|¦package org.snakeyaml.engine.test_suite
            ¦|¦
            ¦|¦/**
            ¦|¦ * Test case for file `./$relativePath`
            ¦|¦ *
            ¦|¦ * See [https://matrix.yaml.info/details/$id.html](https://matrix.yaml.info/details/$id.html)
            ¦|¦ */
            ¦|¦internal object $objectName: YamlTestData.Error {
            ¦|¦
            ¦|¦  override val id: YamlTestData.Id = YamlTestData.Id("${id}")
            ¦|¦
            ¦|¦  /** The name/label of the test */
            ¦|¦  // language=text
            ¦|¦  override val label: String =
            ¦|¦${label.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The YAML input to be parsed or loaded */
            ¦|¦  // language=YAML
            ¦|¦  override val inYaml: String =
            ¦|¦${inYaml.tripleQuoted()}
            ¦|¦}
        """.trimMargin(/* language=text */ "¦|¦")
    }

    private fun generateExpectSuccessData(
        objectName: String,
        id: String,
        testCaseDir: File,
    ): String {
        // === -- The name/label of the test
        val label = testCaseDir.resolve("===").takeIf { it.exists() }?.readText()
            ?: error("missing === label in $testCaseDir")
        // in.yaml -- The YAML input to be parsed or loaded
        val inYaml = testCaseDir.resolve("in.yaml").takeIf { it.exists() }?.readText()
            ?: error("missing in.yaml in $testCaseDir")
        // out.yaml -- The most normal output a dumper would produce
        val outYaml = testCaseDir.resolve("out.yaml").takeIf { it.exists() }?.readText()
        // emit.yaml -- Output an emitter would produce
        val emitYaml = testCaseDir.resolve("emit.yaml").takeIf { it.exists() }?.readText()
        // in.json -- The JSON value that shoiuld load the same as in.yaml
        val inJson = testCaseDir.resolve("in.json").takeIf { it.exists() }?.readText()
        // test.event -- The event DSL produced by the parser test program
        val testEvent = testCaseDir.resolve("test.event").takeIf { it.exists() }?.readText()
            ?: error("missing test.event in $testCaseDir")

        val relativePath = testCaseDir.relativeTo(rootDir).invariantSeparatorsPath

        return /* language=kotlin */ """
            ¦|¦package org.snakeyaml.engine.test_suite
            ¦|¦
            ¦|¦/**
            ¦|¦ * Test case for file `./$relativePath`
            ¦|¦ *
            ¦|¦ * See [https://matrix.yaml.info/details/$id.html](https://matrix.yaml.info/details/$id.html)
            ¦|¦ */
            ¦|¦internal object $objectName: YamlTestData.Success {
            ¦|¦
            ¦|¦  override val id: YamlTestData.Id = YamlTestData.Id("$id")
            ¦|¦
            ¦|¦  /** The name/label of the test */
            ¦|¦  // language=text
            ¦|¦  override val label: String =
            ¦|¦${label.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The YAML input to be parsed or loaded */
            ¦|¦  // language=YAML
            ¦|¦  override val inYaml: String =
            ¦|¦${inYaml.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The most normal output a dumper would produce */
            ¦|¦  // language=YAML
            ¦|¦  override val outYaml: String? =
            ¦|¦${outYaml?.tripleQuoted()}
            ¦|¦
            ¦|¦  /** Output an emitter would produce */
            ¦|¦  // language=text
            ¦|¦  override val emitYaml: String? =
            ¦|¦${emitYaml?.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The JSON value that shoiuld load the same as in.yaml */
            ¦|¦  // language=JSON
            ¦|¦  override val inJson: String? =
            ¦|¦${inJson?.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The event DSL produced by the parser test program */
            ¦|¦  // language=text
            ¦|¦  override val testEvent: String =
            ¦|¦${testEvent.tripleQuoted()}
            ¦|¦}
        """.trimMargin(/* language=text */ "¦|¦")
    }

    companion object {

        @Subst("\"\"\"")
        private const val TRIPLE_QUOTE = /* language=text */ "\"\"\""

        private fun String.tripleQuoted(): String = /* language=text */ """
                ¦|¦$TRIPLE_QUOTE
                ¦|¦${this.lineSequence().joinToString("\n") { "¦$it" }}
                ¦|¦$TRIPLE_QUOTE.trimMargin(/*language=text*/ "¦")
            """.trimMargin("¦|¦")
    }
}
