package it.krzeminski.snakeyaml.engine.kmp.internal

//region Identity hash code
internal actual fun identityHashCode(any: Any?): IdentityHashCode {
    if (any == null) return IdentityHashCode(0)
    val ref = any.toJsReference()
    if (!identityHashCodes.has(ref)) {
        identityHashCodes[ref] = lastIdentityHashCodeId++
    }
    val hc = identityHashCodes[ref]
    return IdentityHashCode(hc)
}

private external interface IdentityHashCodeMap {
    operator fun get(key: JsReference<Any>): Int
    operator fun set(key: JsReference<Any>, value: Int)
    fun has(key: JsReference<Any>): Boolean
}

private val identityHashCodes: IdentityHashCodeMap = js("new WeakMap()")

private var lastIdentityHashCodeId = 0
//endregion
