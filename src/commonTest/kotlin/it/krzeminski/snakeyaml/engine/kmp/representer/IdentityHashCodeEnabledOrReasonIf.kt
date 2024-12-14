package it.krzeminski.snakeyaml.engine.kmp.representer

import io.kotest.common.Platform
import io.kotest.common.platform
import io.kotest.core.test.Enabled
import io.kotest.core.test.EnabledOrReasonIf

internal val identityHashCodeEnabledOrReasonIf: EnabledOrReasonIf = {
    when (platform) {
        Platform.JVM, Platform.Native -> Enabled.enabled
        Platform.JS, Platform.WasmJs  -> Enabled.disabled("identity hashcode does not work correctly")
    }
}
