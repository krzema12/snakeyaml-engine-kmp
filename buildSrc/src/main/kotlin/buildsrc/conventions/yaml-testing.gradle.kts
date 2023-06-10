package buildsrc.conventions

import buildsrc.conventions.Yaml_testing_gradle.Constants.TRIPLE_QUOTE
import buildsrc.conventions.Yaml_testing_gradle.Constants.YAML_TEST_TASK_GROUP
import buildsrc.utils.asConsumer
import buildsrc.utils.dropDirectories
import org.gradle.kotlin.dsl.support.serviceOf
import org.intellij.lang.annotations.Subst

plugins {
    id("buildsrc.conventions.base")
}


object Constants {
    const val YAML_TEST_TASK_GROUP = "yaml testing"

    @Subst("\"\"\"")
    const val TRIPLE_QUOTE = /* language=text */ "\"\"\""
}

val yamlTestSuite by configurations.registering {
    description = "download YAML Test Suite zip"

    asConsumer()

    withDependencies {
        add(project.dependencies.create("yaml:yaml-test-suite:2022-01-17@zip"))
    }
}

// TODO use the file-based test data, not the YAML based data.
//      The YAML based data requires parsing and find/replacment on characters, which is
//      not well documented which leads to bugs in tests. The file-based data is more
//      stable and will likely lead to better test results.

val downloadYamlTestSuite by tasks.registering(Sync::class) {
    group = YAML_TEST_TASK_GROUP
    from(
        yamlTestSuite.map { conf ->
            conf.incoming.artifacts.resolvedArtifacts.map { artifacts ->
                val testDataZip = artifacts.singleOrNull()?.file
                    ?: error("expected exactly one YAML test suite zip, but got ${artifacts.map { it.file }}")
                zipTree(testDataZip)
            }
        },
    ) {
        include("yaml-test-suite*/src/*.y?ml") // match .yaml and .yml files in the src dir
        eachFile {
            relativePath = relativePath
                .dropDirectories(2) // drop dirs: `yaml-test-suite-$TAG/src/`
                .prepend("yaml-test-suite")
        }
    }
    into(temporaryDir)
}

val generateYamlTestSuiteData by tasks.registering {
    group = YAML_TEST_TASK_GROUP

    outputs.dir(temporaryDir)

    val yamlTestSuiteFiles = downloadYamlTestSuite.map { downloader ->
        fileTree(downloader.destinationDir).files
    }
    inputs.files(yamlTestSuiteFiles).withPropertyName("yamlTestSuiteFilePaths")

    val fs = serviceOf<FileSystemOperations>()

    val rootDir = project.rootDir

    doLast("generate files") {
        fs.delete { delete(temporaryDir) }
        temporaryDir.mkdirs()

        val yamlTestSuiteFilesById = yamlTestSuiteFiles.get().associateBy {
            "DATA_${it.nameWithoutExtension}"
        }

        yamlTestSuiteFilesById.forEach { (id, testCaseFile) ->
            val contents = testCaseFile.readText()
            val relativePath = testCaseFile.relativeTo(rootDir).invariantSeparatorsPath

            val destFile = temporaryDir.resolve("$id.kt")

            destFile.writeText(/* language=kotlin */ """
                ¦package org.snakeyaml.engine.test_suite
                ¦
                ¦/** Test case for file `./$relativePath` */
                ¦// language=YAML
                ¦internal const val $id = $TRIPLE_QUOTE
                ¦$contents
                ¦$TRIPLE_QUOTE
                ¦
            """.trimMargin(/* language=text */ "¦")
            )
        }

        val allTestSuiteIdListContents = yamlTestSuiteFilesById.entries
            .map { (id, file) ->
                val suiteId = /* language=kotlin */ """
                    SuiteId("${file.nameWithoutExtension}")
                """.trimIndent()
                "$suiteId to $id,"
            }
            .sorted()
            .joinToString("\n")
            .prependIndent("  ")

        val yamlTestSuiteValuesFile = temporaryDir.resolve("yamlTestSuiteValues.kt")

        yamlTestSuiteValuesFile.writeText(/* language=kotlin */ """
            ¦package org.snakeyaml.engine.test_suite
            ¦
            ¦import org.snakeyaml.engine.test_suite.SuiteDataIdentifier.SuiteId
            ¦
            ¦/** All YAML test suite data */
            ¦internal val yamlTestSuiteData: Map<SuiteId, String> = mapOf(
            ¦$allTestSuiteIdListContents
            ¦)
            ¦
        """.trimMargin(/* language=text */ "¦")
        )
    }
}

val yamlTestSuiteDataSources by tasks.registering(Sync::class) {
    group = YAML_TEST_TASK_GROUP
    into(layout.buildDirectory.dir("generated-sources/src/commonTest/kotlin"))
    from(generateYamlTestSuiteData)
}
