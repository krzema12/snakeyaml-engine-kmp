package buildsrc.tasks

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.intellij.lang.annotations.Language
import org.intellij.lang.annotations.Subst

/**
 * Generate Kotlin code that contains the
 * [YAML Test Suite data](https://github.com/yaml/yaml-test-suite).
 *
 * This is necessary for accessing test data in Kotlin Multiplatform tests, because there's
 * currently no easy way to access and traverse filesystem content in Kotlin Multiplatform code.
 */
@CacheableTask
abstract class GenerateYamlTestSuiteData @Inject constructor(
    private val fs: FileSystemOperations
) : DefaultTask() {

    /** The directory that will contain the generated Kotlin code. */
    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    /** Directory containing the YAML Test Suite data. */
    @get:InputDirectory
    @get:PathSensitive(RELATIVE)
    abstract val yamlTestSuiteFilesDir: DirectoryProperty

    /** Directory of the current project. Used to relativize file paths in generated code. */
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
            .filter { it.isDirectory && it.findSuiteName() != null }

        // for each test suite directory, create a
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

        yamlTestSuiteDirsById.forEach { testCase ->
            // error -- This file indicates the YAML should fail to parse (the file's content is ignored)
            val expectError = testCase.dir.resolve("error").exists()

            val contents = if (expectError) {
                generateExpectErrorData(testCase)
            } else {
                generateExpectSuccessData(testCase)
            }

            val destFile = destination.resolve("${testCase.objectName}.kt")

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
            ¦package it.krzeminski.snakeyaml.engine.kmp.test_suite
            ¦
            ¦/** All YAML test suite data */
            ¦internal object YamlTestSuiteData : Map<YamlTestData.Id, YamlTestData> by mapOf(
            ¦$allTestSuiteIdListContents
            ¦)
            ¦
        """.trimMargin(/* language=text */ "¦")
        )
    }

    /** Data specific to a single YAML Test Suite case. */
    private data class YamlTestSuiteDirSpec(
        val id: String,
        val dir: File,
        val objectName: String = "DATA_${id.replace(":", "_")}",
    ) {
        @Language("http-url-reference")
        val yamlInfoUrl: String = "https://matrix.yaml.info/details/${id}.html"

        /** The name/label of the test */
        val label: String
            get() = dir.findSuiteName()
                ?: error("missing === label in $dir")

        /** `in.yaml` -- The YAML input to be parsed or loaded */
        val inYaml: String
            get() = dir.resolve("in.yaml").readTextOrNull()
                ?: error("missing in.yaml in $dir")
    }

    /**
     * Generate a Kotlin Object for accessing test case data for a case that is expected to fail.
     */
    private fun generateExpectErrorData(case: YamlTestSuiteDirSpec): String {
        // parse the data in the directory...

        val relativePath = case.dir.relativeTo(rootDir).invariantSeparatorsPath

        return /* language=kotlin */ """
            ¦|¦package it.krzeminski.snakeyaml.engine.kmp.test_suite
            ¦|¦
            ¦|¦/**
            ¦|¦ * Test case for file `./$relativePath`
            ¦|¦ *
            ¦|¦ * See [${case.yamlInfoUrl}](${case.yamlInfoUrl})
            ¦|¦ */
            ¦|¦internal object ${case.objectName}: YamlTestData.Error {
            ¦|¦
            ¦|¦  override val id: YamlTestData.Id = YamlTestData.Id("${case.id}")
            ¦|¦
            ¦|¦  /** The name/label of the test */
            ¦|¦  // language=text
            ¦|¦  override val label: String =
            ¦|¦${case.label.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The YAML input to be parsed or loaded */
            ¦|¦  // language=YAML
            ¦|¦  override val inYaml: String =
            ¦|¦${case.inYaml.tripleQuoted()}
            ¦|¦}
        """.trimMargin(/* language=text */ "¦|¦")
    }

    /**
     * Generate a Kotlin Object for accessing test case data for a case that is expected to succeed.
     */
    private fun generateExpectSuccessData(case: YamlTestSuiteDirSpec): String {
        // parse the data in the directory...

        // out.yaml -- The most normal output a dumper would produce
        val outYaml = case.dir.resolve("out.yaml").readTextOrNull()
        // emit.yaml -- Output an emitter would produce
        val emitYaml = case.dir.resolve("emit.yaml").readTextOrNull()
        // in.json -- The JSON value that shoiuld load the same as in.yaml
        val inJson = case.dir.resolve("in.json").readTextOrNull()
        // test.event -- The event DSL produced by the parser test program
        val testEvent = case.dir.resolve("test.event").readTextOrNull()
            ?: error("missing test.event in ${case.dir}")

        val relativePath = case.dir.relativeTo(rootDir).invariantSeparatorsPath

        return /* language=kotlin */ """
            ¦|¦package it.krzeminski.snakeyaml.engine.kmp.test_suite
            ¦|¦
            ¦|¦/**
            ¦|¦ * Test case for file `./$relativePath`
            ¦|¦ *
            ¦|¦ * See [${case.yamlInfoUrl}](${case.yamlInfoUrl})
            ¦|¦ */
            ¦|¦internal object ${case.objectName}: YamlTestData.Success {
            ¦|¦
            ¦|¦  override val id: YamlTestData.Id = YamlTestData.Id("${case.id}")
            ¦|¦
            ¦|¦  /** The name/label of the test */
            ¦|¦  // language=text
            ¦|¦  override val label: String =
            ¦|¦${case.label.tripleQuoted()}
            ¦|¦
            ¦|¦  /** The YAML input to be parsed or loaded */
            ¦|¦  // language=YAML
            ¦|¦  override val inYaml: String =
            ¦|¦${case.inYaml.tripleQuoted()}
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

        /**
         * Get the YAML Test Suite name (the contents of the file named `===`), or `null` if the file does not exist.
         *
         * @receiver must be a directory
         */
        private fun File.findSuiteName(): String? {
            val nameFile = resolve("===")
            return nameFile.readTextOrNull()
        }

        /** Read the text content of the file, if it exists. Else, return `null`. */
        private fun File.readTextOrNull(): String? = if (exists()) readText() else null


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
