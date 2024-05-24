package it.krzeminski.snakeyaml.engine.kmp.internal
//
//internal actual fun getEnvironmentVariable(key: String): String? {
//    // TODO how can environment variables be implemented in Kotlin/Wasm?
//    return null
//}


//region Identity hash code
internal actual fun identityHashCode(any: Any?): IdentityHashCode {
    if (any == null) return IdentityHashCode(0)
    val ref = any.toJsReference()
    if (ref !in identityHashCodes) {
        identityHashCodes[ref] = lastIdentityHashCodeId++
    }
    val hc = identityHashCodes[ref]
    return IdentityHashCode(hc)
}

private external interface IdentityHashCodeMap {
    operator fun get(x: JsReference<Any>): Int
    operator fun set(x: JsReference<Any>, value: Int): Unit
    operator fun contains(x: JsReference<Any>): Boolean
}

private val identityHashCodes: IdentityHashCodeMap = js("new WeakMap()")

private var lastIdentityHashCodeId = 0
//endregion
