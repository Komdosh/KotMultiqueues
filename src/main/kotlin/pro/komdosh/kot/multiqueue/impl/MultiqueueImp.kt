package pro.komdosh.kot.multiqueue.impl

import kotlinx.coroutines.sync.Mutex
import pro.komdosh.kot.multiqueue.api.Multiqueue
import java.util.*

class MultiqueueImp<T : Comparable<T>>(
    private val numOfThreads: Int = 2,
    private val numOfQueuesPerThread: Int = 2,
    private val numOfQueues: Int = numOfThreads * numOfQueuesPerThread,
    private val internalQueues: List<PriorityQueue<T>> = MutableList<PriorityQueue<T>>(numOfQueues) { PriorityQueue() },
    private val locks: Array<Mutex> = Array(numOfQueues) { Mutex() }
) : Multiqueue<T> {

    override fun printSize() {
        internalQueues.forEachIndexed { i, q ->
            println("Queue $i has size ${q.size}")
        }
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

    fun getRandomQueueIndexHalf(): Int {
        return Random().nextInt(numOfQueues / 2)
    }

    private fun getRandomQueueIndex(): Int {
        return Random().nextInt(numOfQueues)
    }

    private fun getQueIndexForDelete(queueIndex: Int, secondQueueIndex: Int): Int? {
        val firstQueue = internalQueues[queueIndex]
        val secondQueue = internalQueues[secondQueueIndex]
        val foundQueueIndex: Int? = if (firstQueue.isEmpty() && secondQueue.isEmpty()) {
            return null
        } else if (firstQueue.isEmpty() && !secondQueue.isEmpty()) {
            secondQueueIndex
        } else {
            findAppropriateIndex(queueIndex, secondQueueIndex)
        }

        return if (foundQueueIndex != null && internalQueues[foundQueueIndex].isEmpty()) {
            null
        } else foundQueueIndex
    }

    private fun findAppropriateIndex(
        firstQueueIndex: Int,
        secondQueueIndex: Int
    ): Int? {
        var foundQueueIndex: Int? = firstQueueIndex
        val value: T? = getValue(firstQueueIndex)
        val value2: T? = getValue(secondQueueIndex)

        if (value2 == null && value == null) {
            foundQueueIndex = null
        } else if (value != null && value2 == null) {
            foundQueueIndex = firstQueueIndex
        } else if (value == null && value2 != null) {
            foundQueueIndex = secondQueueIndex
        }

        if (value2 != null && value != null && value2 > value) {
            foundQueueIndex = secondQueueIndex
        }
        return foundQueueIndex
    }

    private fun getValue(foundQueueIndex: Int): T? {
        while (!locks[foundQueueIndex].tryLock());
        var value: T? = null
        val priorityQueue = internalQueues[foundQueueIndex]
        if (!priorityQueue.isEmpty())
            value = priorityQueue.peek()
        locks[foundQueueIndex].unlock()
        return value
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

    override suspend fun deleteMax(): T? {
        var queueIndex: Int
        var secondQueueIndex: Int
        do {
            queueIndex = getRandomQueueIndex()
            secondQueueIndex = getRandomQueueIndex()
            queueIndex = getQueIndexForDelete(queueIndex, secondQueueIndex) ?: -1
        } while (queueIndex == -1 || !locks[queueIndex].tryLock())
        return getTopValue(queueIndex)
    }

    override suspend fun deleteMaxByThreadId(threadId: Int): T {
        TODO("not implemented")
    }

    override suspend fun deleteMaxByThreadOwn(threadId: Int): T {
        TODO("not implemented")
    }

    private fun getTopValue(queueIndex: Int): T? {
        var topValue: T? = null
        if (!internalQueues[queueIndex].isEmpty()) {
            topValue = internalQueues[queueIndex].peek()
            internalQueues[queueIndex].poll()
        }
        locks[queueIndex].unlock()
        return topValue
    }
}
