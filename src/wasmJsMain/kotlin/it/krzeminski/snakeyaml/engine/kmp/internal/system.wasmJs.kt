package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun getEnvironmentVariable(key: String): String? {
    // TODO how can environment variables be implemented in Kotlin/Wasm?
    return null
}



//region Identity hash code
// https://github.com/korlibs/korge/blob/60af53460dd2d68b6ac86cf459d82434e74be629/kds/src/jsMain/kotlin/korlibs/datastructure/internal/InternalJs.kt
// TODO I miss dynamic :(

//@JsName("Symbol")
//private external fun symbol(name: String): dynamic

//private var lastIdentityHashCodeId = 0
//private val IDENTITY_HASH_CODE_SYMBOL = symbol("KotlinIdentityHashCode")

internal actual fun identityHashCode(any: Any?): IdentityHashCode {
    if (any == null) return IdentityHashCode(0)

    return IdentityHashCode(0)
//    val dyn = any as JsAny
////    dyn.toThrowableOrNull()
//    if (dyn[IDENTITY_HASH_CODE_SYMBOL] === undefined) {
//        dyn[IDENTITY_HASH_CODE_SYMBOL] = lastIdentityHashCodeId++
//    }
//    val hc = dyn[IDENTITY_HASH_CODE_SYMBOL].unsafeCast<Int>()
//    return IdentityHashCode(hc)
}
//endregion
