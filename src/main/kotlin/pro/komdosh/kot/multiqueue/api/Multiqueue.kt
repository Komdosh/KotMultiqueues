package pro.komdosh.kot.multiqueue.api

interface Multiqueue<T : Comparable<T>> {
    fun printSize()

    fun getSize(): Long

    suspend fun balance()

    suspend fun insert(el: T)
    suspend fun insertByThreadId(el: T, threadId: Int)

    suspend fun deleteMax(): T?
    suspend fun deleteMaxByThreadId(threadId: Int): T?
    suspend fun deleteMaxByThreadOwn(threadId: Int): T?
}