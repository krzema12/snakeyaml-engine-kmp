package buildsrc.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import java.nio.file.Path
import javax.inject.Inject

/**
 * Generate Kotlin code that contains the test resources from the common source set.
 *
 * This is necessary for accessing test data in Kotlin Multiplatform tests, because there's
 * currently no easy way to access and traverse filesystem content in Kotlin Multiplatform code.
 */
@CacheableTask
abstract class ConvertCommonTestResourcesToKotlin @Inject constructor(
    private val fs: FileSystemOperations,
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(RELATIVE)
    abstract val commonResourcesDir: DirectoryProperty

    /** The directory that will contain the generated Kotlin code. */
    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @TaskAction
    fun action() {
        val destination = destination.asFile.get()
        fs.delete { delete(destination) }
        destination.mkdirs()

        val resourcesMap = buildResourcesMap()
        val code = generateKotlinCode(resourcesMap)

        destination.resolve("CommonTestResources.kt").writeText(code)
    }

    private fun buildResourcesMap(): Map<String, Any> {
        val resourcesMap = mutableMapOf<String, Any>()

        commonResourcesDir.asFile.get().walk()
            .filter { it.isFile }
            .forEach { file ->
                val relativePath = file.relativeTo(commonResourcesDir.asFile.get()).toPath()
                relativePath.toList().let { pathSegments: List<Path> ->
                    val mapToAddFileTo = pathSegments
                        .dropLast(1)
                        .fold(resourcesMap) { acc, path ->
                            @Suppress("UNCHECKED_CAST")
                            acc.getOrPut(path.toString(), { mutableMapOf<String, Any>() }) as MutableMap<String, Any>
                        }
                    mapToAddFileTo[pathSegments.last().toString()] = file.readBytes()
                }
            }

        return resourcesMap
    }

    private fun generateKotlinCode(resourcesMap: Map<String, Any>): String {
        val stringBuilder = StringBuilder()
        generateFunctions(resourcesMap, stringBuilder)
        return """
            import okio.ByteString

            object CommonTestResources {
                val resourcesMap: Map<String, Any> = ${getFunctionName("")}()

                $stringBuilder
            }
        """.trimIndent()
    }

    private fun generateFunctions(map: Map<String, Any>, stringBuilder: StringBuilder, path: String = "") {
        stringBuilder.append(generateSingleFunction(map, path))
        for ((key, value) in map) {
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                generateFunctions(map[key] as Map<String, Any>, stringBuilder, "$path/$key")
            }
        }
    }

    private fun generateSingleFunction(map: Map<String, Any>, path: String): String {
        val functionName = getFunctionName(path)
        return map.entries.joinToString(
            separator = ",\n",
            prefix = "fun ${functionName}() = mapOf(\n",
            postfix = "\n)",
        ) { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is ByteArray -> "\"$key\" to ByteString.of(${value.joinToString(separator = ", ") {
                    "0x${"%02x".format(it)}.toByte()"
                }})"
                is Map<*, *> -> "\"$key\" to ${getFunctionName("$path/$key")}()"
                else -> error("Unexpected type: $value")
            }
        }
    }

    private val pathToFunctionName: MutableMap<String, String> = mutableMapOf()

    private fun getFunctionName(path: String): String =
        pathToFunctionName.getOrPut(path) { "function${pathToFunctionName.size}" }
}
