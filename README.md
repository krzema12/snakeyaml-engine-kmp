[![Maven Central Version](https://maven-badges.herokuapp.com/maven-central/it.krzeminski/snakeyaml-engine-kmp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/it.krzeminski/snakeyaml-engine-kmp)

[![Commits to upstream](https://raw.githubusercontent.com/krzema12/snakeyaml-engine-kmp/refs/heads/commits-to-upstream-badge/commits-to-upstream-badge.svg)](https://raw.githubusercontent.com/krzema12/snakeyaml-engine-kmp/refs/heads/commits-to-upstream-badge/log-diff-between-repos.txt) - the number of commits in snakeyaml-engine to be considered as candidates for porting to snakeyaml-engine-kmp

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
* Kotlin/Native,
* Kotlin/Wasm.

SnakeYAML Engine KMP is based on
[SnakeYAML Engine](https://bitbucket.org/snakeyaml/snakeyaml-engine/).

> [!TIP]
> Are you wondering if SnakeYAML Engine KMP is for you?
>
> SnakeYAML Engine KMP offers fine-grained control and advanced functionality,
> which is a good fit if you need more customization and flexibility.
>
> If you're for a hassle-free YAML (de)serialization experience, we recommend using
> [kaml](https://github.com/charleskorn/kaml),
> which is designed for simplicity and ease of integration.

## Status

The library has been successfully ported to KMP, and is consumed by [kaml](https://github.com/charleskorn/kaml),
a library that integrates with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).

SnakeYAML Engine KMP uses comprehensive [YAML test suites](https://github.com/yaml/yaml-test-suite)
that validate correctness. Detailed performance testing wasn't conducted, however we have performance
reports (see [here](https://krzema12.github.io/snakeyaml-engine-kmp-benchmarks/dev/bench/)) and mechanisms
in place that should prevent performance regressions.

## How to use

SnakeYAML Engine KMP is published to
[Maven Central](https://search.maven.org/artifact/it.krzeminski/snakeyaml-engine-kmp).

```kts
// build.gradle.kts

repositories {
    mavenCentral()
}

dependencies {
    implementation("it.krzeminski:snakeyaml-engine-kmp:x.y.z")
}
```

To learn about the API, view the [reference documentation](https://krzema12.github.io/snakeyaml-engine-kmp/).

We also recommend reading
[SnakeYAML Engine's documentation](https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Documentation).

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
