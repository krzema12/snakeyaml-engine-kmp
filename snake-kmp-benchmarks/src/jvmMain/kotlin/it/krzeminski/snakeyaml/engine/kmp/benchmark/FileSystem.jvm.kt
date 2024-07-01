package it.krzeminski.snakeyaml.engine.kmp.benchmark

import okio.FileSystem

actual fun fileSystem(): FileSystem = FileSystem.SYSTEM
