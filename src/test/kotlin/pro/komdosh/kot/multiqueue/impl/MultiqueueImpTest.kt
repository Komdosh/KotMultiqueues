package pro.komdosh.kot.multiqueue.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pro.komdosh.kot.multiqueue.api.Multiqueue

internal class MultiqueueImpTest {

    private val multiqueue: Multiqueue<Int> = MultiqueueImp(numOfThreads = 6)
    private val length = 5L
    private val values: Array<Int> = Array(length.toInt()) { i -> i + 1 }

    @BeforeEach
    fun setUp() {
        for (value in values) {
            multiqueue.insert(value)
        }
    }

    @Test
    fun getSize() {
        multiqueue.insert(0)
        multiqueue.insert(1)
        multiqueue.insert(2)
        assert(multiqueue.getSize() == length + 3)
    }

    @Test
    fun balance() {
        multiqueue.balance()
        assert(multiqueue.getSize() == length)
    }

    @Test
    fun insert() {
        multiqueue.insert(0)
        assert(multiqueue.getSize() == length + 1)

        multiqueue.insert(0)
        assert(multiqueue.getSize() == length + 2)

        multiqueue.insert(0)
        assert(multiqueue.getSize() == length + 3)
    }

    @Test
    fun insertByThreadId() {
        multiqueue.insertByThreadId(0, 1)
        assert(multiqueue.getSize() == length + 1)

        multiqueue.insertByThreadId(0, 2)
        assert(multiqueue.getSize() == length + 2)

        multiqueue.deleteMax()
        multiqueue.insertByThreadId(0, 3)
        assert(multiqueue.getSize() == length + 2)
    }

    @Test
    fun deleteMax() {
        val value = multiqueue.deleteMax()

        assert(values.contains(value))
        assert(multiqueue.getSize() == length - 1)
    }

    @Test
    fun deleteMaxByThreadId() {
        val value = multiqueue.deleteMaxByThreadId(1)

        assert(values.contains(value))
        assert(multiqueue.getSize() == length - 1)
    }

    @Test
    fun deleteMaxByThreadOwn() {
        val value = multiqueue.deleteMaxByThreadOwn(2)


        assert(values.contains(value))
        assert(multiqueue.getSize() == length - 1)
    }
}