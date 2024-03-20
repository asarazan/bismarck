package net.sarazan.bismarck.freshness

import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.platform.readString
import net.sarazan.bismarck.platform.writeString

class PersistentFreshness(path: String, ms: Long) : SimpleFreshness(ms) {

    private val file = File(path)

    init {
        if (file.exists) {
            val split = file.readString().split(":")
            lastRunNanos = split[0].toLong()
            resetNanos = split[1].toLong()
        }
    }

    override fun save() {
        file.writeString("$lastRunNanos:$resetNanos")
    }
}
