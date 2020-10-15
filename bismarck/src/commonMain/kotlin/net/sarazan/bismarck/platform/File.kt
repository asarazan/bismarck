package net.sarazan.bismarck.platform

internal expect class File {
    constructor(path: String)
    constructor(parent: String, child: String)
    constructor(parent: File, child: String)

    val parentFile: File?
    val exists: Boolean

    fun delete(): Boolean
    fun mkdirs(): Boolean
    fun createNewFile(): Boolean

    fun readBytes(): ByteArray
    fun writeBytes(bytes: ByteArray)
}

@OptIn(ExperimentalStdlibApi::class)
internal fun File.readString(): String = readBytes().decodeToString()
@OptIn(ExperimentalStdlibApi::class)
internal fun File.writeString(value: String) = writeBytes(value.encodeToByteArray())
