plugins {
    kotlin("multiplatform")
    kotlin("plugin.allopen") version "2.0.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.11"
}

allOpen {
    // JMH required all benchmark classes to be open
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
                implementation(project.dependencies.platform("com.squareup.okio:okio-bom:3.9.0"))
                implementation(projects.snakeyamlEngineKmp)
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.11")
                implementation("com.squareup.okio:okio")
            }
        }

        jsMain {
            dependencies {
                implementation("net.thauvin.erik.urlencoder:urlencoder-lib:1.5.0") {
                    because("error during benchmark generation: e:\n" +
                        "KLIB resolver: Could not find \"net.thauvin.erik.urlencoder:urlencoder-lib\" in [.../snakeyaml-engine-kmp]")
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
                layout.projectDirectory
                    .file("data/issues/kmp-issue-204-OpenAI-API.yaml")
                    .asFile
                    .relativeTo(layout.projectDirectory.asFile),
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
