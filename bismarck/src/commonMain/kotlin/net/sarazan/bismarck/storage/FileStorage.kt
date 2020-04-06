package net.sarazan.bismarck.storage

import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.serialization.Serializer

class FileStorage<T : Any>(
    val path: String,
    val serializer: Serializer<T>
) : MemoryStorage<T>() {

    override fun get(): T? {
        val cached = super.get()
        if (cached != null) return cached
        val file = File(path)
        if (!file.exists) return null
        val loaded = serializer.deserialize(file.readBytes())
        super.put(loaded)
        return loaded
    }

    override fun put(data: T?) {
        super.put(data)
        val file = File(path)
        if (data == null) {
            file.delete()
        } else {
            val parent = file.parentFile
            if (parent?.exists == false) {
                parent.mkdirs()
            }
            if (!file.exists) {
                file.createNewFile()
            }
            file.writeBytes(serializer.serialize(data))
        }
    }
}
