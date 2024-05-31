package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun identityHashCode(any: Any?): IdentityHashCode {
    // Relies on implementation detail in Kotlin/Wasi: hashCode() returns identity hash code
    // https://github.com/JetBrains/kotlin/blob/155eb9b77ad7a87d582fd80534ec400bc7f812b0/libraries/stdlib/wasm/builtins/kotlin/Any.kt#L43-L45
    val hc = any?.hashCode() ?: 0
    return IdentityHashCode(hc)
}
