package pro.komdosh.kot.multiqueue.impl

import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.jetbrains.kotlinx.lincheck.verifier.quiescent.QuiescentConsistencyVerifier
import org.junit.jupiter.api.Test
import pro.komdosh.kot.multiqueue.api.Multiqueue

@StressCTest(verifier = QuiescentConsistencyVerifier::class, threads = 1, iterations = 2)
internal class QuiescentTest : VerifierState() {
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
    fun main() {
        LinChecker.check(QuiescentTest::class.java)
    }

    @Override
    override fun extractState(): Any {
        return multiqueue
    }
}

