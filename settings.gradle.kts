rootProject.name = "snakeyaml-engine-kmp"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()

        // Declare the Node.js & Yarn download repositories
        // https://youtrack.jetbrains.com/issue/KT-55620/
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "NodeJsDistributions"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("org.nodejs") }
        }

        ivy("https://github.com/") {
            name = "GitHub Release"
            // used to download YAML Test Suite data from GitHub
            patternLayout {
                artifact("[organization]/[module]/archive/[revision].[ext]")
                artifact("[organization]/[module]/archive/refs/tags/[revision].[ext]")
                artifact("[organization]/[module]/archive/refs/tags/v[revision].[ext]")
            }
            metadataSources { artifact() }
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
