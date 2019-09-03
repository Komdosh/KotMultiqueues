import pro.komdosh.kot.multiqueue.api.Multiqueue
import pro.komdosh.kot.multiqueue.impl.MultiqueueImp

fun main() {
    val multiqueue: Multiqueue<Int> = MultiqueueImp(numOfThreads = 6)
    val length = 5L
    val values: Array<Int> = Array(length.toInt()) { i -> i + 1 }

    for (value in values) {
        multiqueue.insert(value)
    }

    println(multiqueue.deleteMax())
}