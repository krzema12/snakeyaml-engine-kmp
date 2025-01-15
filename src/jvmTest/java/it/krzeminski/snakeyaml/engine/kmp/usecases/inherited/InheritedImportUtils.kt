package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Source
import org.snakeyaml.engine.v2.utils.TestUtils

private const val PATH =  "/inherited_yaml_1_1"

fun getResource(theName: String): String =
    TestUtils.getResource("$PATH/$theName")

@JvmOverloads
fun getStreamsByExtension(
    extension: String,
    onlyIfCanonicalPresent: Boolean = false,
): List<Path> =
    "src/jvmTest/resources$PATH".toPath().also {
        require(FileSystem.SYSTEM.exists(it)) { "Folder not found: $it" }
        require(FileSystem.SYSTEM.metadataOrNull(it)?.isDirectory == true)
    }.let {
        FileSystem.SYSTEM.listRecursively(it)
            .filter { FileSystem.SYSTEM.inheritedFilenameFilter(it, extension, onlyIfCanonicalPresent) }
            .toList()
    }

fun getFileByName(name: String): Path =
    "src/jvmTest/resources$PATH/$name".toPath().also {
        require(FileSystem.SYSTEM.exists(it)) { "File not found: $it" }
        require(FileSystem.SYSTEM.metadataOrNull(it)?.isRegularFile == true)
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

private fun FileSystem.inheritedFilenameFilter(
    path: Path,
    extension: String,
    onlyIfCanonicalPresent: Boolean,
): Boolean {
    val name = path.name
    val position = name.lastIndexOf('.')
    val canonicalFileName = name.substring(0, position) + ".canonical"
    val canonicalFilePath = path.parent?.resolve(canonicalFileName)
        ?: error("Canonical file path does not exist: $path")
    return if (onlyIfCanonicalPresent && !this.exists(canonicalFilePath)) {
        false
    } else {
        name.endsWith(extension)
    }
}
