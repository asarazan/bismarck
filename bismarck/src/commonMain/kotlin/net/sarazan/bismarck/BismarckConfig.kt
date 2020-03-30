package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import net.sarazan.bismarck.fetch.Fetch
import net.sarazan.bismarck.persisters.MemoryPersister

data class BismarckConfig<T : Any>(
    var fetch: Fetch<T>? = null,
    var rateLimiter: RateLimiter? = null,
    var persister: Persister<T> = MemoryPersister(),
    var scope: CoroutineScope = GlobalScope,
    var debug: Boolean = false
)
