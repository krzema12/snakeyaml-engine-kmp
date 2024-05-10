import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse

fun main() {
    val parser = Parse(LoadSettings.builder().build())

    parser.parseInputStream(System.`in`).forEach { event -> println(event) }
}
