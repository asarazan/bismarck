package net.sarazan.bismarck.platform

import java.io.File as JavaFile

internal actual class File(private val file: JavaFile) {

    actual constructor(path: String) : this(JavaFile(path))

    actual constructor(parent: String, child: String) :
            this(JavaFile(parent, child))

    actual constructor(parent: File, child: String) :
            this(JavaFile(parent.file, child))

    actual val parentFile: File? get() = File(file.parentFile)
    actual val exists: Boolean get() = file.exists()

    actual fun delete() = file.delete()
    actual fun mkdirs() = file.mkdirs()
    actual fun createNewFile() = file.createNewFile()
    actual fun readBytes() = file.readBytes()
    actual fun writeBytes(bytes: ByteArray) = file.writeBytes(bytes)
}
