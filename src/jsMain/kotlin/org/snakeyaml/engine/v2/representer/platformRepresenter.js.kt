package org.snakeyaml.engine.v2.representer

import org.snakeyaml.engine.v2.api.DumpSettings

actual fun Representer(settings: DumpSettings): Representer {
    return CommonRepresenter(settings)
}
