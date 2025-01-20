package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.Buffer
import okio.ByteString
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.buffer
import org.intellij.lang.annotations.Language

/**
 * Retrieves a string content from common resources using a given path.
 *
 * @param path The slash-delimited path to the resource, with a leading slash.
 * @return The content of the resource as a UTF-8 encoded string with normalized line breaks.
 * @throws IllegalArgumentException if the resource doesn't exist.
 */
fun stringFromResources(@Language("file-reference") path: String): String {
    require(path.startsWith("/")) { "A leading slash is required!" }
    val path = path.drop(1).toPath()
    return CommonTestResourcesFileSystem.source(path).buffer().readUtf8()
        // In this particular library, we produce uniform line breaks (\n)
        // on all OSes, hence it's desired to normalize them even if in
        // the resource file, we have a different kind of line breaks.
        .replace(Regex("\\r\\n?"), "\n")
}

/**
 * Exposes files stored in `commonTest/resources` as a [FileSystem].
 */
val CommonTestResourcesFileSystem = buildFileSystem(CommonTestResources.resourcesMap)

/**
 * Returns an instance of [FileSystem] that can traverse files using a resource map
 * included in a generated accessor class. One has to configure the build logic to generate
 * the accessor class for a given directory tree.
 */
internal fun buildFileSystem(resourcesMap: Map<String, Any>): FileSystem =
    GenericCommonTestResourcesFileSystem(resourcesMap)

private class GenericCommonTestResourcesFileSystem(private val resourcesMap: Map<String, Any>) : FileSystem() {
    override fun list(dir: Path): List<Path> =
        listOrNull(dir) ?: throw IOException("Cannot list $dir")

    override fun listOrNull(dir: Path): List<Path>? {
        val node = traverseResourcesMap(resourcesMap, dir) ?: return null
        if (node !is Map<*, *>) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return (node as Map<String, Any?>).keys.map { dir.resolve(it) }
    }

    override fun metadataOrNull(path: Path): FileMetadata? =
        traverseResourcesMap(resourcesMap, path)?.let { node ->
            FileMetadata(
                isDirectory = node is Map<*, *>,
                isRegularFile = node !is Map<*, *>,
            )
        }

    override fun source(file: Path): Source =
        traverseResourcesMap(resourcesMap, file)?.let {
            if (it !is ByteString) {
                throw IOException("'$file' is not a file")
            }
            (Buffer().write(it) as Source)
        } ?: throw IOException("File '$file' doesn't exist")

    override fun canonicalize(path: Path): Path =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun openReadOnly(file: Path): FileHandle =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun sink(file: Path, mustCreate: Boolean): Sink =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun appendingSink(file: Path, mustExist: Boolean): Sink =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun createDirectory(dir: Path, mustCreate: Boolean) =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun atomicMove(source: Path, target: Path) =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun delete(path: Path, mustExist: Boolean) =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")

    override fun createSymlink(source: Path, target: Path) =
        throw NotImplementedError("This operation is not supported by this simple implementation of the file system.")
}

/**
 * Returns a map or a leaf (file) in the resources map, pointed to by [path],
 * or `null` if the file or directory cannot be found.
 */
private fun traverseResourcesMap(resourcesMap: Map<String, Any>, path: Path): Any? {
    var map: Any? = resourcesMap
    for (segment in path.segments) {
        if (map is Map<*, *>) {
            map = map[segment] ?: return null
        } else {
            return null
        }
    }
    return map
}
