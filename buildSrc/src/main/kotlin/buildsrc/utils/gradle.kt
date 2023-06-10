package buildsrc.utils

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.file.RelativePath
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.*

/**
 * Mark this [Configuration] as one that will be consumed by other subprojects.
 *
 * ```
 * isCanBeResolved = false
 * isCanBeConsumed = true
 * ```
 */
fun Configuration.asProvider(
    visible: Boolean = true
) {
    isVisible = visible
    isCanBeResolved = false
    isCanBeConsumed = true
}

/**
 * Mark this [Configuration] as one that will consume artifacts from other subprojects (also known as 'resolving')
 *
 * ```
 * isCanBeResolved = true
 * isCanBeConsumed = false
 * ```
 * */
fun Configuration.asConsumer(
    visible: Boolean = false
) {
    isVisible = visible
    isCanBeResolved = true
    isCanBeConsumed = false
}


/** Drop the first [count] directories from the path */
fun RelativePath.dropDirectories(count: Int): RelativePath =
    RelativePath(true, *segments.drop(count).toTypedArray())


/** Drop the first directory from the path */
fun RelativePath.dropDirectoriesWhile(
    segmentPrediate: (segment: String) -> Boolean
): RelativePath =
    RelativePath(
        true,
        *segments.dropWhile(segmentPrediate).toTypedArray(),
    )
