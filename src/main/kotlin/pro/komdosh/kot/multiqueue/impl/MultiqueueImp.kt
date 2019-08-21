package pro.komdosh.kot.multiqueue.impl

import kotlinx.coroutines.sync.Mutex
import pro.komdosh.kot.multiqueue.api.Multiqueue
import java.util.*

class MultiqueueImp<T>(
    private val numOfThreads: Int = 2,
    private val numOfQueuesPerThread: Int = 2,
    private val numOfQueues: Int = numOfThreads * numOfQueuesPerThread,
    private val internalQueues: List<PriorityQueue<T>> = listOf(),
    private val locks: Array<Mutex> = Array(numOfQueues) { Mutex() }
) : Multiqueue<T> {

    override fun printSize() {
        TODO("not implemented")
    }

    override fun getSize(): Int {
        TODO("not implemented")
    }

    override suspend fun balance() {
        TODO("not implemented")
    }

    override suspend fun insert(el: T) {
        TODO("not implemented")
    }

    override suspend fun insertByThreadId(el: T, threadId: Int) {
        TODO("not implemented")
    }

    override suspend fun deleteMax(): T {
        TODO("not implemented")
    }

    override suspend fun deleteMaxByThreadId(threadId: Int): T {
        TODO("not implemented")
    }

    override suspend fun deleteMaxByThreadOwn(threadId: Int): T {
        TODO("not implemented")
    }
}
