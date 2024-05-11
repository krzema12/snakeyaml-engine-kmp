package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import okio.Source

actual class Compose actual constructor(
    settings: LoadSettings,
) {
    private val common = ComposeCommon(settings)

    actual fun compose(source: Source): Node? = common.compose(source)

    actual fun compose(string: String): Node? = common.compose(string)

    actual fun composeAll(source: Source): Iterable<Node> = common.composeAll(source)

    actual fun composeAll(string: String): Iterable<Node> = common.composeAll(string)

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    actual fun composeString(yaml: String): Node? = compose(yaml)

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    actual fun composeAllFromString(yaml: String): Iterable<Node> = composeAll(yaml)
}
