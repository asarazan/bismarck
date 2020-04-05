package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineScope
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.ratelimit.Freshness
import net.sarazan.bismarck.storage.MemoryStorage
import net.sarazan.bismarck.storage.Storage

typealias Fetcher<T> = suspend () -> T?

data class BismarckConfig<T : Any>(
    var fetcher: Fetcher<T>? = null,
    var freshness: Freshness? = null,
    var storage: Storage<T> = MemoryStorage(),
    var scope: CoroutineScope = CoroutineScope(BismarckDispatchers.default),
    var checkOnLaunch: Boolean = false
)
