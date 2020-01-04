package net.sarazan.bismarck

import kotlin.reflect.KClass

expect annotation class Throws(vararg val exceptionClasses: KClass<out Throwable>)

expect fun platformName(): String

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}

