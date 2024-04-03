package net.sarazan.bismarck.extensions

import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckConfigMarker
import net.sarazan.bismarck.freshness.PersistentFreshness
import net.sarazan.bismarck.freshness.SimpleFreshness
import net.sarazan.bismarck.serialization.Serializer
import net.sarazan.bismarck.storage.FileStorage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@BismarckConfigMarker
class FilesConfig<T : Any> {
    lateinit var path: String
    lateinit var serializer: Serializer<T>
}

@BismarckConfigMarker
class FreshnessConfig {
    var duration: Duration = (15 * 60 * 1000).milliseconds
    var path: String? = null
}

fun <T : Any> Bismarck.Config<T>.fileStorage(init: FilesConfig<T>.() -> Unit) {
    val files = FilesConfig<T>().apply(init)
    storage = FileStorage(files.path, files.serializer)
}

fun <T : Any> Bismarck.Config<T>.freshness(init: FreshnessConfig.() -> Unit) {
    val fresh = FreshnessConfig().apply(init)
    freshness = when (val it = fresh.path) {
        null -> SimpleFreshness(fresh.duration, timeSource)
        else -> PersistentFreshness(it, fresh.duration, timeSource)
    }
}
