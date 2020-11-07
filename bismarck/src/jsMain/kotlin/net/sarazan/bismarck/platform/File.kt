package net.sarazan.bismarck.platform

import fs.MakeDirectoryOptions
import org.w3c.dom.url.URL

val fsHack = js("require(\"fs\")")
val mkdirRecursive = js("{ recursive: true }").unsafeCast<MakeDirectoryOptions>()
fun sanitizeUrlPath(url: String): String {
    return when {
        url.startsWith("http:") ||
        url.startsWith("https:") -> throw RuntimeException("Cannot handle remote URLs for now")
        url.startsWith("file:") -> url
        else -> "file://${js("__dirname")}/$url"
    }
}

internal actual class File(val url: URL) {

    actual constructor(path: String) : this(URL(sanitizeUrlPath(path)))

    actual constructor(parent: String, child: String) : this("${parent}/${child}")

    actual constructor(parent: File, child: String) : this(parent.url.href, child)

    actual val parentFile: File?
        get() = url.parent()?.let { File(it) }

    actual val exists: Boolean
        get() = fs.existsSync(url)

    actual fun delete(): Boolean {
        if (!exists) return true
        return try {
            fs.unlinkSync(url)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun mkdirs(): Boolean {
        return try {
            fs.mkdirSync(url, mkdirRecursive)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun createNewFile(): Boolean {
        return try {
            fsHack.openSync(url, "w")
            true
        } catch (e: Error) {
            false
        }
    }

    actual fun readBytes(): ByteArray {
        val str = fs.readFileSync(url, "utf8")
        return str.encodeToByteArray()
    }

    actual fun writeBytes(bytes: ByteArray) {
        fs.writeFileSync(url, bytes.decodeToString(), "utf8")
    }
}