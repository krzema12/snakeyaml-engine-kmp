package it.krzeminski.snakeyaml.engine.kmp.scanner

internal class ReadOnlyIntArrayToListAdapter(
    private val array: IntArray,
) : AbstractList<Int>() {

    override val size: Int
        get() = array.size

    override fun get(index: Int): Int = array[index]
}

internal fun IntArray.toReadOnlyList(): List<Int> {
    return when (size) {
        0 -> emptyList()
        1 -> listOf(get(0))
        else -> ReadOnlyIntArrayToListAdapter(this)
    }
}
