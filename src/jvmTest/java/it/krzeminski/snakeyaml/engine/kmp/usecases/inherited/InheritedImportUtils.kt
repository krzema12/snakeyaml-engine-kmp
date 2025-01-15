package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.source
import org.snakeyaml.engine.v2.utils.TestUtils
import java.io.File
import java.io.FilenameFilter
import java.io.InputStream

private const val PATH =  "/inherited_yaml_1_1"

fun getResource(theName: String): String =
    TestUtils.getResource("$PATH/$theName")

@JvmOverloads
fun getStreamsByExtension(
    extension: String,
    onlyIfCanonicalPresent: Boolean = false,
): Array<File> =
    File("src/jvmTest/resources$PATH").also {
        require(it.exists()) { "Folder not found: ${it.absolutePath}" }
        require(it.isDirectory)
    }.listFiles(InheritedFilenameFilter(extension, onlyIfCanonicalPresent))

fun getFileByName(name: String): File =
    File("src/jvmTest/resources$PATH/$name").also {
        require(it.exists()) { "Folder not found: ${it.absolutePath}" }
        require(it.isFile)
    }

fun canonicalParse(input2: InputStream, label: String): List<Event> {
    val settings = LoadSettings.builder().setLabel(label).build()
    val reader = StreamReader(settings, YamlUnicodeReader(input2.source()))
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
    input2.close()
    return result
}

fun parse(input: InputStream): List<Event> {
    val settings = LoadSettings.builder().build()
    val reader = StreamReader(settings, YamlUnicodeReader(input.source()))
    val parser = ParserImpl(settings, reader)
    val result = buildList {
        while (parser.hasNext()) {
            add(parser.next())
        }
    }
    input.close()
    return result
}

private class InheritedFilenameFilter(
    private val extension: String,
    private val onlyIfCanonicalPresent: Boolean,
) : FilenameFilter {
    override fun accept(dir: File?, name: String): Boolean {
        val position = name.lastIndexOf('.')
        val canonicalFileName = name.substring(0, position) + ".canonical"
        val canonicalFile = File(dir, canonicalFileName)
        return if (onlyIfCanonicalPresent && !canonicalFile.exists()) {
            false
        } else {
            name.endsWith(extension)
        }
    }
}
