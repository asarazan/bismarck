package net.sarazan.bismarck.platform

import kotlin.jvm.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return "JVM"
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun currentTimeNano(): Long = System.nanoTime()

actual class ObservableLike<T>
actual class SubscriberLike<T> {
    actual fun onNext(data: T?) {
        TODO()
    }
}