package buildsrc.conventions.lang

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.LockStoreTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

val kotlinNodeJs = extensions.getByType<NodeJsRootExtension>()

// Normalize paths from Windows separators to Unix in NPM package.json and package-lock.json
// Workaround for https://youtrack.jetbrains.com/issue/KT-67442
tasks.withType<KotlinNpmInstallTask>().configureEach {
    val packageJson = kotlinNodeJs.rootPackageDirectory.map { it.file("package.json") }
    val packageLockJson = kotlinNodeJs.rootPackageDirectory.map { it.file("package-lock.json") }

    doLast {
        fun fixNpmPackageJson(f: File) {
            f.writeText(
                f.readText()
                    .replace(
                        """"packages\\snakeyaml-engine-kmp"""",
                        """"packages/snakeyaml-engine-kmp"""",
                    )
                    .replace(
                        """"packages\\snakeyaml-engine-kmp-test"""",
                        """"packages/snakeyaml-engine-kmp-test"""",
                    )
                    .replace(
                        """"packages_imported\\kotlin-test-js-runner\\0.0.1"""",
                        """"packages_imported/kotlin-test-js-runner/0.0.1"""",
                    )
            )
        }

        packageJson.orNull?.asFile?.let { fixNpmPackageJson(it) }
        packageLockJson.orNull?.asFile?.let { fixNpmPackageJson(it) }
    }
}

tasks.withType<LockStoreTask>().configureEach {
    doFirst {
        // pretty-print the lockfile diff, for easier debugging if verification fails on CI
        val inputFile = inputFile.orNull?.asFile.takeIf { it?.exists() == true }
        val outputFile = outputDirectory.orNull?.asFile?.resolve(fileName.orNull ?: "")?.takeIf { it.exists() }

        if (inputFile != null && outputFile != null) {
            val checkText = inputFile.readText()
            val builtText = outputFile.readText()

            // compare line-by-line to disregard Windows/Unix line separators
            val checkLines = checkText.lines()
            val builtLines = builtText.lines()
            if (checkLines != builtLines) {
                val patch = DiffUtils.diff(checkLines, builtLines)
                val diff = UnifiedDiffUtils.generateUnifiedDiff(
                    inputFile.invariantSeparatorsPath,
                    outputFile.invariantSeparatorsPath,
                    checkLines,
                    patch,
                    3,
                ).joinToString("\n\n")
                logger.lifecycle(diff)
            }
        }
    }
}
