package it.krzeminski.snakeyaml.engine.kmp.representer

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings

actual fun Representer(settings: DumpSettings): Representer {
    return CommonRepresenter(settings)
}
