package pro.komdosh.kot.multiqueue.impl

import kotlinx.coroutines.sync.Mutex
import pro.komdosh.kot.multiqueue.api.Multiqueue
import java.util.*

class MultiqueueImp<T>(
    private val numOfThreads: Int = 2,
    private val numOfQueuesPerThread: Int = 2,
    private val numOfQueues: Int = numOfThreads * numOfQueuesPerThread,
    private val internalQueues: List<PriorityQueue<T>> = MutableList<PriorityQueue<T>>(numOfQueues) { PriorityQueue() },
    private val locks: Array<Mutex> = Array(numOfQueues) { Mutex() }
) : Multiqueue<T> {

    override fun printSize() {
        TODO("not implemented")
    }

    override fun getSize(): Long {
        var numOfElements: Long = 0;
        for (i in internalQueues) {
            numOfElements += i.size
        }

        return numOfElements;
    }

    override suspend fun balance() {
        TODO("not implemented")
    }

    fun getRandomQueueIndex(): Int {
        return Random().nextInt(numOfQueues / 2)
    }

    override suspend fun insert(el: T) {
        var queueIndex: Int
        do {
            queueIndex = getRandomQueueIndex()
        } while (!locks[queueIndex].tryLock())
        internalQueues[queueIndex].add(el)
        locks[queueIndex].unlock()
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
