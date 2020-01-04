package com.jetbrains.handson.mpp.mobile

import kotlin.jvm.Throws

actual typealias Throws = Throws

actual fun platformName(): String {
    return "Android"
}
