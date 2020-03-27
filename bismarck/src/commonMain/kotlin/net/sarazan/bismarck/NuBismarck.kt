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
    var persister: Persister<T>? = null,
    var scope: CoroutineScope? = null
)

@ExperimentalCoroutinesApi
class NuBismarck<T : Any>(config: NuBismarckConfig<T>.() -> Unit = {}) : Closeable {

    val fetcher: Fetcher<T>?
    val rateLimiter: RateLimiter?
    val persister: Persister<T>
    val scope: CoroutineScope

    var value: T?
        get() = persister.get()
        private set(value) {
            persister.put(value)
            scope.launch {
                valueChannel.send(value)
            }
        }
    val valueChannel: ConflatedBroadcastChannel<T?>

    val state: BismarckState get() = when {
        fetcher != null && fetchCount.get() > 0 -> BismarckState.Fetching
        isFresh -> BismarckState.Fresh
        else -> Stale
    }
    val stateChannel: ConflatedBroadcastChannel<BismarckState>

    val isFresh: Boolean get() {
        return rateLimiter?.isFresh() ?: false
    }

    private val fetchCount = AtomicInt(0)

    init {
        NuBismarckConfig<T>().apply(config).let {
            fetcher = it.fetcher
            rateLimiter = it.rateLimiter
            persister = it.persister ?: MemoryPersister()
            scope = it.scope ?: GlobalScope
        }
        valueChannel = ConflatedBroadcastChannel(value)
        stateChannel = ConflatedBroadcastChannel(state)
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