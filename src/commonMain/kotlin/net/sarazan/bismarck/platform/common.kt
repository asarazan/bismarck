package net.sarazan.bismarck.platform

import kotlin.reflect.KClass

expect class ObservableLike<T> {

}

expect class SubscriberLike<T> {
    fun onNext(data: T?)
}

expect fun currentTimeMillis(): Long
expect fun currentTimeNano(): Long

expect annotation class Throws(vararg val exceptionClasses: KClass<out Throwable>)

expect fun platformName(): String

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}

