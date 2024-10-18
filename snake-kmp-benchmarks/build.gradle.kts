plugins {
    kotlin("multiplatform")
    kotlin("plugin.allopen") version "2.0.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.12"
}

allOpen {
    // JMH requires all benchmark classes to be open
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    jvmToolchain(11)

    //region JVM Targets
    jvm()
    //endregion

    //region JS target
    js(IR) {
        nodejs()
    }
    //endregion

    //region Native Targets
    // According to https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier 3
    mingwX64()
    //endregion

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.snakeyamlEngineKmp)
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.12")
                implementation(project.dependencies.platform("com.squareup.okio:okio-bom:3.9.1"))
                implementation("com.squareup.okio:okio")
            }
        }

        jvmMain {
            dependencies {
                implementation("org.snakeyaml:snakeyaml-engine:2.8")
            }
        }

        jsMain {
            dependencies {
                implementation("net.thauvin.erik.urlencoder:urlencoder-lib:1.6.0-SNAPSHOT") {
                    because("benchmark compilation requires explicit dependencies for JS targets - https://github.com/Kotlin/kotlinx-benchmark/issues/185")
                }
                implementation("com.squareup.okio:okio-nodefilesystem")
            }
        }
    }
}

benchmark {
    configurations {
        getByName("main") {
            iterations = 10
            iterationTime = 5
            iterationTimeUnit = "s"
            param(
                "openAiYamlPath",
                // JS target requires an absolute path. Otherwise, file cannot be found.
                layout.projectDirectory
                    .file("data/issues/kmp-issue-204-OpenAI-API.yaml")
                    .asFile
                    .absolutePath,
            )
        }
    }
    targets {
        register("jvm")
        register("js")
        register("macosX64")
        register("macosArm64")
        register("iosX64")
        register("iosArm64")
        register("iosSimulatorArm64")
        register("linuxX64")
        register("mingwX64")
    }
}
