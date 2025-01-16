package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.Buffer
import okio.ByteString
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer

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

private fun traverseResourcesMap(path: Path): Any? {
    return try {
        path.segments
            .fold(Pair(CommonTestResources.resourcesMap as Any?, "/resources/")) { (map, pathSoFar), node ->
                if (map is Map<*, *>) {
                    require(node in map) {
                        "Node '$node' doesn't exist in directory '$pathSoFar'!"
                    }
                    Pair(map[node], "$pathSoFar$node/")
                } else {
                    error("It shouldn't happen!")
                }
            }.first
    } catch (e: Exception) {
        // TODO: hack, reimplement
        null
    }
}

val CommonTestResourcesFileSystem = object : FileSystem() {
    override fun list(dir: Path): List<Path> =
        (traverseResourcesMap(dir) as Map<String, Any?>).keys
            .map { dir.resolve(it) }

    override fun listOrNull(dir: Path): List<Path>? =
        // TODO: definitely needs to be implemented in a cleaner way
        try {
            list(dir)
        } catch (_: Exception) {
            null
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
