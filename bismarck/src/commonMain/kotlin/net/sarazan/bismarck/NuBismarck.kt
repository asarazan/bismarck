package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import net.sarazan.bismarck.BismarckState.Stale
import net.sarazan.bismarck.impl.Fetcher
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.platform.Closeable

data class NuBismarckConfig<T : Any>(
    var fetcher: Fetcher<T>? = null,
    var rateLimiter: RateLimiter? = null,
    var persister: Persister<T> = MemoryPersister(),
    var scope: CoroutineScope = GlobalScope
)

@ExperimentalCoroutinesApi
class NuBismarck<T : Any>(config: NuBismarckConfig<T>.() -> Unit = {}) : Closeable {

    private val config = NuBismarckConfig<T>().apply(config)
    val fetcher get() = this.config.fetcher
    val rateLimiter get() = this.config.rateLimiter
    val persister get() = this.config.persister
    val scope get() = this.config.scope

    var value: T?
        get() = persister.get()
        private set(value) {
            persister.put(value)
            scope.launch {
                valueChannel.send(value)
            }
        }

    val state: BismarckState get() = when {
        fetcher != null && fetchCount.get() > 0 -> BismarckState.Fetching
        isFresh -> BismarckState.Fresh
        else -> Stale
    }

    val isFresh: Boolean get() {
        return rateLimiter?.isFresh() ?: false
    }

    private val fetchCount = AtomicInt(0)

    val valueChannel by lazy { ConflatedBroadcastChannel(value) }
    val stateChannel by lazy { ConflatedBroadcastChannel(state) }

    init {
        scope.launch {
            valueChannel.send(value)
            stateChannel.send(state)
        }
    }

    fun insert(value: T?) {
        this.value = value
        rateLimiter?.update()
        updateState()
    }

    fun invalidate() {
        rateLimiter?.reset()
        fetch()
    }

    override fun close() {
        valueChannel.close()
        stateChannel.close()
    }

    private fun fetch() {
        fetchCount.incrementAndGet()
        updateState()
        scope.launch {
            val fetched = fetcher?.invoke()
            insert(fetched)
            fetchCount.decrementAndGet()
            updateState()
        }
    }

    private fun updateState() {
        scope.launch {
            stateChannel.send(state)
        }
    }
}