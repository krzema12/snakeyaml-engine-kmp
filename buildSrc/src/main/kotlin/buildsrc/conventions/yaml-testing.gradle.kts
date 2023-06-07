package buildsrc.conventions

import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

plugins {
    id("buildsrc.conventions.base")
}

val yamlTestSuite by configurations.registering {
    description = "download YAML Test Suite zip"

    isCanBeResolved = true
    isCanBeConsumed = false
    isVisible = false

    withDependencies {
        add(project.dependencies.create("yaml:yaml-test-suite:2022-01-17@zip"))
    }
}

val downloadYamlTestSuite by tasks.registering(Sync::class) {
    group = "yaml testing"
    from(
        yamlTestSuite.map { conf ->
            conf.incoming.artifacts.resolvedArtifacts.map { artifacts ->
                val testDataZip = artifacts.singleOrNull()?.file
                    ?: error("expected exactly one YAML test suite zip")
                zipTree(testDataZip)
            }
        },
    )
    into(temporaryDir)
}
