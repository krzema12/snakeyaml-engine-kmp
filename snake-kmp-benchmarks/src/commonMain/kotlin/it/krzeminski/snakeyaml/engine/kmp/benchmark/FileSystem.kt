package it.krzeminski.snakeyaml.engine.kmp.benchmark

import okio.FileSystem

/**
 * Because of JS (and Wasm) target it is required to have this method
 * to access the file system in the common code
 */
expect fun fileSystem(): FileSystem
