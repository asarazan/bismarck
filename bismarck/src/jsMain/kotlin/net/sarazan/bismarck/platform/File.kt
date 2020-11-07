package net.sarazan.bismarck.platform

internal actual class File {
    val path: String

    actual constructor(path: String) {
        this.path = path
    }

    actual constructor(parent: String, child: String) {
        TODO("Not yet implemented")
    }

    actual constructor(parent: File, child: String) {
        TODO("Not yet implemented")
    }

    actual val parentFile: File?
        get() = TODO("Not yet implemented")
    actual val exists: Boolean
        get() = TODO("Not yet implemented")

    actual fun delete(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun mkdirs(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun createNewFile(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun readBytes(): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun writeBytes(bytes: ByteArray) {
    }
}