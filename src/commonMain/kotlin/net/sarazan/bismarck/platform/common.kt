package net.sarazan.bismarck.platform

import kotlin.reflect.KClass

expect fun currentTimeMillis(): Long

expect annotation class Throws(vararg val exceptionClasses: KClass<out Throwable>)

expect fun platformName(): String

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}

