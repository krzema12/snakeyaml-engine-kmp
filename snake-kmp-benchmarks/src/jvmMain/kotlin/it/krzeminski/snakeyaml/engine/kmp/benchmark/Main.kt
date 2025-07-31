import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.lowlevel.Compose


fun testYaml(withComments: Boolean) {
    val contents = """
- lowerBound: !type:AlarmThresholdSetting
    # Actual low pressure damage threshold is at 20 kPa, but below ~85 kPa you can't breathe due to lack of oxygen.
    threshold: 85
	""".trimIndent()

    val settings = LoadSettings.builder()
        .setParseComments(withComments)
        .build()
    val composer = Compose(settings)
    composer.composeString(contents)
}

fun main() {
    testYaml(false)
    println("without comments: pass")
    testYaml(true) // crashes here
    println("with comments: pass")
}
