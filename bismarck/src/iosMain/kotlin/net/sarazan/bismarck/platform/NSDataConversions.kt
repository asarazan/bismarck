package net.sarazan.bismarck.platform

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

@ExperimentalUnsignedTypes
fun NSData.toByteArray(): ByteArray = ByteArray(this@toByteArray.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
}

fun ByteArray.toNSData(): NSData = memScoped {
    val string = NSString.create(string = this@toNSData.decodeToString())
    return string.dataUsingEncoding(NSUTF8StringEncoding)!!
}
