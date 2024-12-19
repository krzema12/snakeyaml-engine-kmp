package it.krzeminski.snakeyaml.engine.kmp.representer

import io.kotest.core.Platform
import io.kotest.core.platform
import io.kotest.core.test.Enabled
import io.kotest.core.test.EnabledOrReasonIf

internal val identityHashCodeEnabledOrReasonIf: EnabledOrReasonIf = {
    when (platform) {
        Platform.JS,
        Platform.WasmJs ->
            Enabled.disabled("identity hashcode does not work correctly: https://github.com/krzema12/snakeyaml-engine-kmp/pull/273")

        else            -> Enabled.enabled
    }
}
