package buildsrc.conventions

import buildsrc.conventions.Yaml_testing_gradle.Constants.YAML_TEST_TASK_GROUP
import buildsrc.utils.asConsumer
import buildsrc.utils.dropDirectories

/**
 * Generate code for accessing YAML Test Suite Data in test source sets, in a
 * KMP-comptaible manner.
 *
 * @see [buildsrc.tasks.GenerateYamlTestSuiteData]
 */

plugins {
    id("buildsrc.conventions.base")
}

private object Constants {
    const val YAML_TEST_TASK_GROUP = "yaml testing"
}

val yamlTestSuite by configurations.registering {
    description = "download YAML Test Suite data"

    asConsumer()

    withDependencies {
        add(project.dependencies.create("yaml:yaml-test-suite:data-2022-01-17@zip"))
    }
}

val downloadYamlTestSuite by tasks.registering(Sync::class) {
    description = "download and unpack YAML Test Suite data"
    group = YAML_TEST_TASK_GROUP
    from(yamlTestSuite.map { conf ->
        conf.incoming.artifacts.resolvedArtifacts.map { artifacts ->
            val testDataZip = artifacts.singleOrNull()?.file
                ?: error("expected exactly one YAML test suite zip, but got ${artifacts.map { it.file }}")
            zipTree(testDataZip)
        }
    }) {
        eachFile {
            // drop dir `yaml-test-suite-data-2022-01-17/`
            relativePath = relativePath.dropDirectories(1)
        }
    }
    into(temporaryDir)
}

val generateYamlTestSuiteData by tasks.registering(buildsrc.tasks.ConvertCommonTestResourcesToKotlin::class) {
    description =
        "generate Kotlin code for accessing the YAML Test Suite data in multiplatform code"
    group = YAML_TEST_TASK_GROUP

    destination.set(temporaryDir)
    commonResourcesDir.fileProvider(downloadYamlTestSuite.map { it.destinationDir })
    destination.set(layout.buildDirectory.dir("generated-code-for-yaml-test-suite/src/commonTest/kotlin"))
    accessorFileAndClassName.set("YamlTestSuiteResources")
}

val yamlTestSuiteDataSources by tasks.registering(Sync::class) {
    description = "lifecycle task for generating all test data source files and resources"
    group = YAML_TEST_TASK_GROUP
    into(layout.buildDirectory.dir("generated-sources/src/commonTest/kotlin"))
    from(generateYamlTestSuiteData)
}
