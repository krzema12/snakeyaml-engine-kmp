package org.snakeyaml.engine.v2.scanner

import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.Optional

// TODO move this into ScannerImpl after Kotlin conversion is complete and make private
internal class BreakIntentHolder(
    @JvmField
    val breaks: String,
    @JvmField
    val maxIndent: Int,
    @JvmField
    val endMark: Optional<Mark>,
)
