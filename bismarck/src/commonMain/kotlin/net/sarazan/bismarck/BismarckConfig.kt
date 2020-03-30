package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import net.sarazan.bismarck.ratelimit.RateLimiter
import net.sarazan.bismarck.storage.MemoryStorage
import net.sarazan.bismarck.storage.Storage

data class BismarckConfig<T : Any>(
    var fetch: Fetch<T>? = null,
    var rateLimiter: RateLimiter? = null,
    var storage: Storage<T> = MemoryStorage(),
    var scope: CoroutineScope = GlobalScope,
    var debug: Boolean = false
)
