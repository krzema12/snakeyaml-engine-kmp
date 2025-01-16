package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.Buffer
import okio.ByteString
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Sink
import okio.Source

/**
 * Retrieves a string content from common resources using a given path.
 *
 * @param path The slash-delimited path to the resource, with a leading slash.
 * @return The content of the resource as a UTF-8 encoded string with normalized line breaks.
 * @throws IllegalArgumentException if the resource doesn't exist.
 */
fun stringFromResources(path: String): String {
    require(path.startsWith("/")) { "A leading slash is required!" }
    val segments = path.drop(1).split("/")
    return (segments
        .dropLast(1)
        .fold(Pair(CommonTestResources.resourcesMap, "/resources/")) { (map, pathSoFar), directory ->
            require(directory in map) {
                "Directory '$directory' doesn't exist in directory '$pathSoFar'!"
            }
            @Suppress("UNCHECKED_CAST")
            Pair(map[directory] as Map<String, Any>, "$pathSoFar$directory/")
        }).let { (map, pathSoFar) ->
            val file = segments.last()
            require(file in map) {
                "File '$file' doesn't exist in directory '$pathSoFar'!"
            }
            map[file] as ByteString
        }.utf8()
        // In this particular library, we produce uniform line breaks (\n)
        // on all OSes, hence it's desired to normalize them even if in
        // the resource file, we have a different kind of line breaks.
        .replace(Regex("\\r\\n?"), "\n")
}

/**
 * Returns a map or a leaf (file) in the resources map, pointed to by [path],
 * or `null` if the file or directory cannot be found.
 */
private fun traverseResourcesMap(path: Path): Any? {
    var map: Any? = CommonTestResources.resourcesMap
    for (segment in path.segments) {
        if (map is Map<*, *>) {
            if (segment in map) {
                map = map[segment]
            } else {
                return null
            }
        } else {
            return null
        }
    }
    return map
}

val CommonTestResourcesFileSystem = object : FileSystem() {
    override fun list(dir: Path): List<Path> =
        listOrNull(dir) ?: throw IOException("Cannot list $dir")

    override fun listOrNull(dir: Path): List<Path>? {
        val node = traverseResourcesMap(dir) ?: return null
        if (node !is Map<*, *>) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return (node as Map<String, Any?>).keys.map { dir.resolve(it) }
    }

    override fun metadataOrNull(path: Path): FileMetadata? =
        traverseResourcesMap(path)?.let { node ->
            FileMetadata(
                isDirectory = node is Map<*, *>,
                isRegularFile = node !is Map<*, *>,
            )
        }

    override fun source(file: Path): Source =
        (Buffer().write(traverseResourcesMap(file) as ByteString) as Source)

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
