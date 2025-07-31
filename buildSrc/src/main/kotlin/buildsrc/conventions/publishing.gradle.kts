package buildsrc.conventions

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
val signingKeyId: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.keyId")
val signingKey: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.key")
val signingPassword: Provider<String> =
    providers.gradleProperty("snake-kmp.signing.password")
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

    signing {
        logger.lifecycle("publishing.gradle.kts enabled signing for ${project.path}")
        if (signingKeyId.isPresent && signingKey.isPresent && signingPassword.isPresent) {
            useInMemoryPgpKeys(signingKeyId.get(), signingKey.get(), signingPassword.get())
        } else {
            useGpgCmd()
        }
    }
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
