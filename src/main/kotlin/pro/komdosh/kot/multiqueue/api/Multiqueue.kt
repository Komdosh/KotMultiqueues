package pro.komdosh.kot.multiqueue.api

interface Multiqueue<T : Comparable<T>> {
    fun printSize()

    fun getSize(): Long

    suspend fun balance()

    fun insert(el: T)
    fun insertByThreadId(el: T, threadId: Int)

    fun deleteMax(): T?
    fun deleteMaxByThreadId(threadId: Int): T?
    fun deleteMaxByThreadOwn(threadId: Int): T?
}
