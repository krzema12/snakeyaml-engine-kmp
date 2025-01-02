package it.krzeminski.snakeyaml.engine.kmp.api

import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
import okio.Source

actual class Load actual constructor(
    settings: LoadSettings,
    constructor: BaseConstructor
) {
    private val common = LoadCommon(settings, constructor)

    actual fun loadOne(string: String): Any? =
        common.loadOne(string)

    internal actual fun loadOne(source: Source): Any? =
        common.loadOne(source)

    actual fun loadAll(string: String): Iterable<Any?> =
        common.loadAll(string)

    internal actual fun loadAll(source: Source): Iterable<Any?> =
        common.loadAll(source)
}
