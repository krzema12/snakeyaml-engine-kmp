package buildsrc.tasks

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.intellij.lang.annotations.Subst

/**
 * Generate Kotlin code that contains the
 * [YAML Test Suite data](https://github.com/yaml/yaml-test-suite).
 *
 * This is helpful for Kotlin Multiplatform testing, because there's currently no easy way to
 * access and traverse files in common code.
 */
@CacheableTask
abstract class GenerateYamlTestSuiteData @Inject constructor(
    private val fs: FileSystemOperations
) : DefaultTask() {

    /**
     * The
     */
    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(RELATIVE)
    abstract val yamlTestSuiteFilesDir: DirectoryProperty

    /** Directory of the current project. Used to relativize file paths in generated code */
    private val rootDir = project.rootDir

    @TaskAction
    fun action() {
        // clear the destination, in case there are any previously generated cases that are now removed
        val destination = destination.asFile.get()
        fs.delete { delete(destination) }
        destination.mkdirs()

        // find all directories with a name file
        val yamlTestSuiteDirs = yamlTestSuiteFilesDir.get().asFile
            .walk()
            .filter { it.isDirectory && it.resolve("===").exists() }

        data class YamlTestSuiteDirSpec(
            val id: String,
            val dir: File,
            val objectName: String = "DATA_${id.replace(":", "_")}",
        )

        val yamlTestSuiteDirsById = yamlTestSuiteDirs.map { dir ->
            val parentDirContents = dir.parentFile.listFiles().orEmpty().asList()

            // Some directories contain a single case, while some contain multiple.
            // Determine a distinct ID for each test case, based on the name and, if there are
            // multiple cases, the case number.

            val dirHasMultipleCases = parentDirContents.all {
                it.isDirectory && it.name.toIntOrNull() != null
            }

            val id =
                if (dirHasMultipleCases) {
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
        // parse the data in the directory...

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
        // parse the data in the directory...

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

        /**
         * Surround this string in triple string quotes
         *
         * So that it looks pretty in code it's indented with a margin, and trimmed using
         * [trimMargin].
         */
        private fun String.tripleQuoted(): String = /* language=text */ """
                ¦|¦$TRIPLE_QUOTE
                ¦|¦${this.lineSequence().joinToString("\n") { "¦$it" }}
                ¦|¦$TRIPLE_QUOTE.trimMargin(/*language=text*/ "¦")
            """.trimMargin("¦|¦")
    }
}
