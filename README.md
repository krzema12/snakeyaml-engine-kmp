# SnakeYAML Engine KMP

## Overview

[SnakeYAML Engine KMP](https://github.com/krzema12/snakeyaml-engine-kmp)
is a 
[YAML](http://yaml.org) 
1.2 processor for
[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html),
and supports

* Kotlin/JS,
* Kotlin/JVM,
* Kotlin/Native.

SnakeYAML Engine KMP is based on
[SnakeYAML Engine](https://bitbucket.org/snakeyaml/snakeyaml-engine/).

## Status

The library has been successfully ported to KMP, and is consumed by [kaml](https://github.com/charleskorn/kaml),
a library that integrates with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).

SnakeYAML Engine KMP uses comprehensive [YAML test suites](https://github.com/yaml/yaml-test-suite) that validate correctness. However, performance has not been evaluated.

## How to use

The library is published to Maven Central. Use the following Maven coords:

```kts
repositories {
  mavenCentral()
}

dependencies {
  implementation("it.krzeminski:snakeyaml-engine-kmp:x.y.z")
}
```

To learn about the API, it's best to start with snakeyaml-engine's (upstream project's) docs [here](https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Documentation).

### Snapshot releases

Snapshot versions of SnakeYAML Engine KMP (straight from the main branch) are available. 
They are published to a GitHub branch, which must be added as a
[custom Gradle Plugin repository](https://docs.gradle.org/current/userguide/plugins.html#sec:custom_plugin_repositories)

```kts
// settings.gradle.kts

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()

    // add the snapshot repository
    maven("https://raw.githubusercontent.com/krzema12/snakeyaml-engine-kmp/artifacts/m2/") {
      name = "SnakeYAML Engine KMP Snapshots"
      mavenContent {
        // only include the relevant snapshots
        includeGroup("it.krzeminski")
        snapshotsOnly()
      }
    }
  }
}
```

Once the repository is configured, add a dependency on the library:

```kts
// build.gradle.kts

dependencies {
    implementation("it.krzeminski:snakeyaml-engine-kmp:x.y.z-SNAPSHOT")
}
```
