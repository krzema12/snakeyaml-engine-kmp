package it.krzeminski.snakeyaml.engine.kmp.internal

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Source

/**
 * Represents a [FileSystem]-aware [Path]. Thanks to this, there's no need
 * to keep writing e.g. `FileSystem.SYSTEM.exists("foobar".toPath())`, and
 * repeat the "file system" part. The file system becomes a part of the path,
 * allowing more concise and abstract code - specifying the file system is
 * required only once, at the beginning of operating on the path.
 */
class FSPath(val path: Path, val fs: FileSystem) {
    val name: String
        get() = path.name

    val parent: FSPath?
        get() = path.parent?.let { FSPath(it, fs) }

    val exists: Boolean
        get() = fs.exists(path)

    val isDirectory: Boolean
        get() = fs.metadataOrNull(path)?.isDirectory == true

    val isRegularFile: Boolean
        get() = fs.metadataOrNull(path)?.isRegularFile == true

    fun listRecursively(): Sequence<FSPath> =
        fs.listRecursively(path)
            .map { FSPath(it, fs) }

    fun resolve(child: String): FSPath =
        FSPath(path.resolve(child), fs)

    fun source(): Source =
        fs.source(path)

    override fun toString(): String =
        path.toString()
}

fun FileSystem.fsPath(path: String): FSPath =
    FSPath(path.toPath(), this)