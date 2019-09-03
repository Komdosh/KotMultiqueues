package pro.komdosh.kot.multiqueue.impl

import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import pro.komdosh.kot.multiqueue.api.Multiqueue
import java.util.concurrent.TimeUnit

@StressCTest
internal class LinearizabityTest : VerifierState() {
    private val multiqueue: Multiqueue<Int> = MultiqueueImp(numOfThreads = 6)

    @Operation
    fun insert(value: Int) {
        return multiqueue.insert(value)
    }

    @Operation
    fun get(): Int? {
        return multiqueue.deleteMax()
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun main() {
        val opts = StressOptions()
            .iterations(10)
            .threads(2)
            .logLevel(LoggingLevel.INFO)
        val thrown = assertThrows<AssertionError> {
            LinChecker.check(LinearizabityTest::class.java, opts)
        }
        assertTrue(thrown.message?.contains("Invalid interleaving found") ?: false)
    }

    @Override
    override fun extractState(): Any {
        return multiqueue
    }
}

