package it.krzeminski.snakeyaml.engine.kmp.benchmark

import okio.FileSystem
import okio.NodeJsFileSystem

/**
 * Without JS and Wasm targets there is no need in this function
 * but we can keep it so minimize change when those targets are added
 */
actual fun fileSystem(): FileSystem = NodeJsFileSystem
