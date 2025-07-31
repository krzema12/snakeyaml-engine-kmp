package buildsrc.conventions

import org.gradle.api.plugins.JavaBasePlugin.DOCUMENTATION_GROUP

/**
 * Conventions for publishing.
 *
 * Mostly focused on Maven Central publishing, which requires
 *
 * * a Javadoc JAR (even if the project is not a Java project)
 * * artifacts are signed (and Gradle's [SigningPlugin] is outdated and does not have good support for lazy config/caching)
 */

plugins {
    signing
    id("com.vanniktech.maven.publish")
}


//region Publication Properties
// can be set in gradle.properties or environment variables, e.g. ORG_GRADLE_PROJECT_snake-kmp.ossrhUsername
val ossrhUsername = providers.gradleProperty("snake-kmp.ossrhUsername")
val ossrhPassword = providers.gradleProperty("snake-kmp.ossrhPassword")
val ossrhStagingRepositoryID = providers.gradleProperty("snake-kmp.ossrhStagingRepositoryId")

val signingKeyId: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.keyId")
val signingKey: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.key")
val signingPassword: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.password")
val signingSecretKeyRingFile: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.secretKeyRingFile")

val isReleaseVersion = provider { !version.toString().endsWith("-SNAPSHOT") }

val sonatypeReleaseUrl = isReleaseVersion.flatMap { isRelease ->
    if (isRelease) {
        ossrhStagingRepositoryID.map { repositoryId ->
            "https://oss.sonatype.org/service/local/staging/deployByRepositoryId/${repositoryId}/"
        }.orElse("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    } else {
        provider { "https://oss.sonatype.org/content/repositories/snapshots/" }
    }
}
//endregion


//region POM convention
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    pom {
        name.convention("SnakeYAML Engine KMP")
        description.convention("SnakeYAML Engine KMP is a YAML 1.2 processor for Kotlin Multiplatform")
        url.convention("https://github.com/krzema12/snakeyaml-engine-kmp/")

        scm {
            connection.convention("scm:git:https://github.com/krzema12/snakeyaml-engine-kmp/")
            developerConnection.convention("scm:git:https://github.com/krzema12/")
            url.convention("https://github.com/krzema12/snakeyaml-engine-kmp")
        }

        licenses {
            license {
                name.convention("Apache-2.0")
                url.convention("https://opensource.org/licenses/Apache-2.0")
            }
        }

        developers {
            developer {
                email.set("adam@adamko.dev")
            }

            developer {
                id.set("krzema12")
                name.set("Piotr Krzemiński")
                email.set("git@krzeminski.it")
            }
        }
    }
}
//endregion


//region Maven Central publishing/signing
val javadocJarStub by tasks.registering(Jar::class) {
    group = DOCUMENTATION_GROUP
    description = "Empty Javadoc Jar (required by Maven Central)"
    archiveClassifier.set("javadoc")
}

if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
    mavenPublishing {
        repositories {
            maven(sonatypeReleaseUrl) {
                name = "SonatypeRelease"
                credentials {
                    username = ossrhUsername.get()
                    password = ossrhPassword.get()
                }
            }
            // Publish to a project-local Maven directory, for verification.
            // To test, run:
            // ./gradlew publishAllPublicationsToProjectLocalRepository
            // and check $rootDir/build/maven-project-local
            maven(rootProject.layout.buildDirectory.dir("maven-project-local")) {
                name = "ProjectLocal"
            }
        }
    }

    signing {
        logger.lifecycle("publishing.gradle.kts enabled signing for ${project.path}")
        if (signingKeyId.isPresent && signingKey.isPresent && signingPassword.isPresent) {
            useInMemoryPgpKeys(signingKeyId.get(), signingKey.get(), signingPassword.get())
        } else {
            useGpgCmd()
        }
    }

    afterEvaluate {
        // Register signatures in afterEvaluate, otherwise the signing plugin creates
        // the signing tasks too early, before all the publications are added.
        signing {
            sign(publishing.publications)
        }
    }
}
//endregion


//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()

tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(signingTasks)
}
//endregion


//region publishing logging
tasks.withType<AbstractPublishToMaven>().configureEach {
    val publicationGAV = provider { publication?.run { "$group:$artifactId:$version" } }
    doLast("log publication GAV") {
        if (publicationGAV.isPresent) {
            logger.lifecycle("[task: ${path}] ${publicationGAV.get()}")
        }
    }
}
//endregion
