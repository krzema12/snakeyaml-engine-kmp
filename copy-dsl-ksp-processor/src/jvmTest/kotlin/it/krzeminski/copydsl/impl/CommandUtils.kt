package it.krzeminski.copydsl.impl

import java.io.File
import java.util.concurrent.TimeUnit

fun runCommand(vararg args: String): String {
    println("Running command: ${args.joinToString(" ")}")
    val proc = ProcessBuilder(args.toList())
        .directory(File("."))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText()
}
