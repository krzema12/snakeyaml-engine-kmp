package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun getEnvironmentVariable(key: String): String? {
    js("return process.env[key]")
}

//region Identity hash code
internal actual fun objectIdentityHashCode(any: Any): IdentityHashCode {
    val ref = any.toJsReference()
    if (!identityHashCodes.has(ref)) {
        identityHashCodes.set(ref, lastIdentityHashCodeId++)
    }
    val hc = identityHashCodes.get(ref)
    return IdentityHashCode(hc)
}

// NOTE: don't add operator modifier to get and set methods
// kotlin transform those method into `map[key]`
// instead of `map.get(key)` and causes exception to be thrown
private external class IdentityHashCodeMap : JsAny {
    fun get(key: JsAny): Int
    fun set(key: JsAny, value: Int)
    fun has(key: JsAny): Boolean
}

private val identityHashCodes: IdentityHashCodeMap = js("new WeakMap()")

// lastIdentityHashCodeId starts from 1 because 0 is a reserved value for `null`
private var lastIdentityHashCodeId = 1
//endregion
