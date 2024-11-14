package it.krzeminski.snakeyaml.engine.kmp.serializer

import it.krzeminski.snakeyaml.engine.kmp.internal.IdentityHashCode
import it.krzeminski.snakeyaml.engine.kmp.internal.identityHashCode

/**
 * A set that compares objects by their identities, not values.
 * It's an attempt to reimplement `Collections.newSetFromMap(new IdentityHashMap<Node, Boolean>())`
 * from the JVM.
 */
internal class IdentitySet<T> {
    private val contents: MutableSet<IdentityHashCode> = mutableSetOf()

    fun add(obj: T) {
        contents.add(identityHashCode(obj))
    }

    fun contains(obj: T): Boolean {
        return contents.contains(identityHashCode(obj))
    }

    fun clear() {
        contents.clear()
    }

    fun remove(obj: T) {
        contents.remove(identityHashCode(obj))
    }
}
