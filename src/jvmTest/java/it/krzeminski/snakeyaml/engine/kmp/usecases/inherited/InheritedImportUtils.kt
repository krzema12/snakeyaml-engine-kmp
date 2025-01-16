package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.internal.FSPath
import it.krzeminski.snakeyaml.engine.kmp.internal.fsPath
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.FileSystem
import okio.Source
import org.snakeyaml.engine.v2.utils.TestUtils

private const val PATH =  "/inherited_yaml_1_1"

fun getResource(theName: String): String =
    TestUtils.getResource("$PATH/$theName")

@JvmOverloads
fun getStreamsByExtension(
    extension: String,
    onlyIfCanonicalPresent: Boolean = false,
): List<FSPath> =
    FileSystem.SYSTEM.fsPath("src/jvmTest/resources$PATH").also {
        require(it.exists) { "Folder not found: $it" }
        require(it.isDirectory)
    }.listRecursively()
        .filter { it.inheritedFilenameFilter(extension, onlyIfCanonicalPresent) }
        .toList()

fun getFileByName(name: String): FSPath =
    FileSystem.SYSTEM.fsPath("src/jvmTest/resources$PATH/$name").also {
        require(it.exists) { "File not found: $it" }
        require(it.isRegularFile)
    }

fun canonicalParse(input2: Source, label: String): List<Event> {
    val settings = LoadSettings.builder().setLabel(label).build()
    input2.use {
        val reader = StreamReader(settings, YamlUnicodeReader(input2))
        val buffer = StringBuffer()
        while (reader.peek() != 0) {
            buffer.appendCodePoint(reader.peek())
            reader.forward()
        }
        val parser = CanonicalParser(buffer.toString().replace(System.lineSeparator(), "\n"), label)
        val result = buildList {
            while (parser.hasNext()) {
                add(parser.next())
            }
        }
        return result
    }
}

fun parse(input: Source): List<Event> {
    val settings = LoadSettings.builder().build()
    input.use {
        val reader = StreamReader(settings, YamlUnicodeReader(input))
        val parser = ParserImpl(settings, reader)
        val result = buildList {
            while (parser.hasNext()) {
                add(parser.next())
            }
        }
        return result
    }
}

private fun FSPath.inheritedFilenameFilter(
    extension: String,
    onlyIfCanonicalPresent: Boolean,
): Boolean {
    val position = name.lastIndexOf('.')
    val canonicalFileName = name.substring(0, position) + ".canonical"
    val canonicalFilePath = this.parent?.resolve(canonicalFileName)
        ?: error("Canonical file path does not exist: $this")
    return if (onlyIfCanonicalPresent && !canonicalFilePath.exists) {
        false
    } else {
        name.endsWith(extension)
    }
}
