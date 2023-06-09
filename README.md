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
[SnakeYAML Engine](https://github.com/krzema12/snakeyaml-engine-kmp).

## Status

⚠️ SnakeYAML Engine KMP is **under development**.

### Snapshot releases

Experimental snapshot versions of SnakeYAML Engine KMP are available. 
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
