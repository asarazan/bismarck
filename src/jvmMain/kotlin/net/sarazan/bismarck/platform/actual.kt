package net.sarazan.bismarck.platform

import rx.Subscriber
import kotlin.jvm.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return "Android"
}

actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual class ObservableLike<T>
actual class SubscriberLike<T> {
    actual fun onNext(data: T?) {
        TODO()
    }
}

actual fun currentTimeNano(): Long {
    return System.nanoTime()
}