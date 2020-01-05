package net.sarazan.bismarck.platform

import kotlin.jvm.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return "Android"
}

actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}