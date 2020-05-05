package pro.komdosh.multiqueue.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
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
        val sizes = IntArray(numOfQueues) { i -> internalQueues[i].size }

        val indexWithMax: Int = sizes.withIndex().maxBy { it.value }!!.index
        val indexWithMin: Int = sizes.withIndex().minBy { it.value }!!.index

        val sumOfSizes: Int = sizes.sum()
        val averageSize = sumOfSizes / numOfQueues
        val sizeCoeff = 1.2
        val transferCoeff = 0.3

        if (sizes[indexWithMax] > averageSize * sizeCoeff) { // if max sized queue is 20% bigger and more than average
            while (!locks[indexWithMax].tryLock());

            runBlocking(Dispatchers.Default) {
                withTimeout(TIMEOUT_IN_MILLIS) {
                    while (!locks[indexWithMin].tryLock());

                    val sizeOfTransfer =
                        (sizes[indexWithMax] * transferCoeff).toInt() // 30% elements transfer to smallest queue
                    for (i in 0..sizeOfTransfer) {
                        internalQueues[indexWithMin].add(internalQueues[indexWithMax].peek())
                        internalQueues[indexWithMax].poll()
                    }
                    locks[indexWithMin].unlock()
                }
            }

            locks[indexWithMax].unlock()
        }
    }

    private fun getRandomQueueIndexHalf(): Int {
        return Random().nextInt(numOfQueues / 2)
    }

    private fun getRandomQueueIndex(): Int {
        return Random().nextInt(numOfQueues)
    }

    private fun getQueueIndexForDelete(queueIndex: Int, secondQueueIndex: Int): Int? {
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

    override fun insert(el: T) {
        var queueIndex: Int
        do {
            queueIndex = getRandomQueueIndex()
        } while (!locks[queueIndex].tryLock())
        internalQueues[queueIndex].add(el)
        locks[queueIndex].unlock()
    }

    override fun insertByThreadId(el: T, threadId: Int) {
        var queueIndex: Int
        do {
            val halfOfThreads = numOfThreads / 2;

            if (threadId < halfOfThreads) {
                queueIndex = getRandomQueueIndexHalf()
            } else {
                queueIndex = getRandomQueueIndexHalf() + numOfQueues / 2;
            }
        } while (!locks[queueIndex].tryLock());
        internalQueues[queueIndex].add(el);
        locks[queueIndex].unlock();
    }

    override fun deleteMax(): T? {
        var queueIndex: Int
        do {
            queueIndex = randomIndexSelection()
        } while (queueIndex == -1 || !locks[queueIndex].tryLock())
        return getTopValue(queueIndex)
    }

    private fun randomIndexSelection(): Int {
        var queueIndex = getRandomQueueIndex()
        var secondQueueIndex: Int
        do {
            secondQueueIndex = getRandomQueueIndex()
        } while (secondQueueIndex == queueIndex)
        queueIndex = getQueueIndexForDelete(queueIndex, secondQueueIndex) ?: -1
        return queueIndex
    }

    override fun deleteMaxByThreadId(threadId: Int): T? {
        var queueIndex: Int
        do {
            queueIndex = getTopRandomHalfIndex(threadId)
        } while (queueIndex == -1 || !locks[queueIndex].tryLock())
        return getTopValue(queueIndex)
    }

    private fun getTopRandomHalfIndex(threadId: Int): Int {
        var queueIndex: Int
        val secondQueueIndex: Int
        val halfOfThreads = numOfThreads / 2
        if (threadId < halfOfThreads) {
            queueIndex = getRandomQueueIndexHalf()
            secondQueueIndex = getRandomQueueIndexHalf()
        } else {
            queueIndex = getRandomQueueIndexHalf() + numOfQueues / 2
            secondQueueIndex = getRandomQueueIndexHalf() + numOfQueues / 2
        }

        queueIndex = getQueueIndexForDelete(queueIndex, secondQueueIndex) ?: -1
        return queueIndex
    }

    override fun deleteMaxByThreadOwn(threadId: Int): T? {
        var queueIndex: Int = threadId * numOfQueuesPerThread
        val secondQueueIndex = threadId * numOfQueuesPerThread + 1
        queueIndex = getQueueIndexForDelete(queueIndex, secondQueueIndex) ?: -1
        if (queueIndex != -1 && locks[queueIndex].tryLock()) {
            return getTopValue(queueIndex)
        }

        do {
            queueIndex = randomIndexSelection()
        } while (queueIndex == -1 || !locks[queueIndex].tryLock())
        return getTopValue(queueIndex)
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

    companion object {
        private const val TIMEOUT_IN_MILLIS: Long = 10000
    }
}
