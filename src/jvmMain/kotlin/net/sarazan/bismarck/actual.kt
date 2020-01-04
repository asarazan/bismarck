package net.sarazan.bismarck

import kotlin.jvm.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return "Android"
}
