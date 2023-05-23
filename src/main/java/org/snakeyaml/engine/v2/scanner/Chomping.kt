package org.snakeyaml.engine.v2.scanner

import java.util.Optional


// TODO move this into ScannerImpl after Kotlin conversion is complete and make private
internal class Chomping(
    @JvmField
    val value: Indicator,
    @JvmField
    val increment: Optional<Int>,
) {
    constructor(indicatorCodePoint: Int, increment: Optional<Int>) : this(parse(indicatorCodePoint), increment)

    internal enum class Indicator {
        STRIP,
        CLIP,
        KEEP
    }

    companion object {
        private fun parse(codePoint: Int): Indicator {
            return when (codePoint) {
                '+'.code      -> Indicator.KEEP
                '-'.code      -> Indicator.STRIP
                Int.MIN_VALUE -> Indicator.CLIP
                else          -> throw IllegalArgumentException("Unexpected block chomping indicator: $codePoint")
            }
        }
    }
}

//internal sealed interface Chomping2 {
//    val increment: Int?
//
//    @JvmInline
//    value class STRIP(override val increment: Int?) : Chomping2
//    @JvmInline
//    value class CLIP(override val increment: Int?) : Chomping2
//    @JvmInline
//    value class KEEP(override val increment: Int?) : Chomping2
//}
//
//internal fun Chomping2(
//    indicatorCodePoint: Int,
//    increment: Int?,
//): Chomping2 {
//    return when (indicatorCodePoint) {
//        '+'.code      -> Chomping2.KEEP(increment)
//        '-'.code      -> Chomping2.STRIP(increment)
//        Int.MIN_VALUE -> Chomping2.CLIP(increment)
//        else          -> throw IllegalArgumentException("Unexpected block chomping indicator: $indicatorCodePoint")
//    }
//}
