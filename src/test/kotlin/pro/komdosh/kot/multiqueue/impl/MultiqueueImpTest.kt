package pro.komdosh.kot.multiqueue.impl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pro.komdosh.kot.multiqueue.api.Multiqueue

internal class MultiqueueImpTest {

    private val multiqueue: Multiqueue<Int> = MultiqueueImp(numOfThreads = 6)
    private val length = 5L
    private val values: Array<Int> = Array(length.toInt()) { i -> i + 1 }

    @BeforeEach
    fun setUp() {
        runBlocking {
            for (value in values) {
                multiqueue.insert(value)
            }
        }
    }

    @Test
    fun getSize() {
        runBlocking {
            multiqueue.insert(0)
            multiqueue.insert(1)
            multiqueue.insert(2)
        }
        assert(multiqueue.getSize() == length + 3)
    }

    @Test
    fun balance() {
        runBlocking { multiqueue.balance() }
        assert(multiqueue.getSize() == length)
    }

    @Test
    fun insert() {
        runBlocking { multiqueue.insert(0) }
        assert(multiqueue.getSize() == length + 1)

        runBlocking { multiqueue.insert(0) }
        assert(multiqueue.getSize() == length + 2)

        runBlocking {
            multiqueue.insert(0)
        }
        assert(multiqueue.getSize() == length + 3)
    }

    @Test
    fun insertByThreadId() {
        runBlocking { multiqueue.insertByThreadId(0, 1) }
        assert(multiqueue.getSize() == length + 1)

        runBlocking { multiqueue.insertByThreadId(0, 2) }
        assert(multiqueue.getSize() == length + 2)

        runBlocking {
            multiqueue.deleteMax()
            multiqueue.insertByThreadId(0, 3)
        }
        assert(multiqueue.getSize() == length + 2)
    }

    @Test
    fun deleteMax() {
        val value = runBlocking {
            for (value in values) {
                multiqueue.insert(value)
            }
            multiqueue.deleteMax()
        }

        assert(values.contains(value))
        assert(multiqueue.getSize() == length - 1)
    }

    @Test
    fun deleteMaxByThreadId() {
        val value = runBlocking {
            multiqueue.deleteMaxByThreadId(1)
        }

        assert(values.contains(value))
        assert(multiqueue.getSize() == length - 1)
    }

    @Test
    fun deleteMaxByThreadOwn() {
        val value = runBlocking {
            multiqueue.deleteMaxByThreadOwn(2)
        }

        assert(values.contains(value))
        assert(multiqueue.getSize() == length - 1)
    }
}