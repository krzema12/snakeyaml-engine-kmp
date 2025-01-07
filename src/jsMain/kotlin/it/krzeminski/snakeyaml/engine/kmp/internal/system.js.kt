package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun getEnvironmentVariable(key: String): String? {
    return process.env[key] as String?
}

private external val process: Process

private external interface Process {
    val env: dynamic
}

//region Identity hash code
// https://github.com/korlibs/korge/blob/60af53460dd2d68b6ac86cf459d82434e74be629/kds/src/jsMain/kotlin/korlibs/datastructure/internal/InternalJs.kt

@JsName("Symbol")
private external fun symbol(name: String): dynamic

// zero is reserved for null
private var lastIdentityHashCodeId = 1
private val IDENTITY_HASH_CODE_SYMBOL = symbol("KotlinIdentityHashCode")

internal actual fun objectIdentityHashCode(any: Any): IdentityHashCode {
    val dyn = any.asDynamic()
    if (dyn[IDENTITY_HASH_CODE_SYMBOL] === undefined) {
        dyn[IDENTITY_HASH_CODE_SYMBOL] = lastIdentityHashCodeId++
    }
    val hc = dyn[IDENTITY_HASH_CODE_SYMBOL].unsafeCast<Int>()
    return IdentityHashCode(hc)
}
//endregion
