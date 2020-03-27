package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import net.sarazan.bismarck.impl.Fetcher
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.platform.Closeable

// TODO - expose publicly
private val scope = GlobalScope

data class NuBismarckConfig<T : Any>(
    var fetcher: Fetcher<T>? = null,
    var rateLimiter: RateLimiter? = null,
    var persister: Persister<T>? = null
)

@ExperimentalCoroutinesApi
class NuBismarck<T : Any>(config: NuBismarckConfig<T>.() -> Unit = {}) : Closeable {

    var fetcher: Fetcher<T>? = null
        private set
    var rateLimiter: RateLimiter? = null
        private set
    var persister: Persister<T> = MemoryPersister()
        private set

    var value: T? = null
        get() = persister.get()
        private set(value) {
            persister.put(value)
            scope.launch {
                valueChannel.send(field)
            }
        }
    val valueChannel = ConflatedBroadcastChannel(value)

    val state: BismarckState? get() = when {
        fetcher != null && fetchCount.get() > 0 -> BismarckState.Fetching
        isFresh -> BismarckState.Fresh
        else -> BismarckState.Stale
    }
    val stateChannel = ConflatedBroadcastChannel(state)

    val isFresh: Boolean get() {
        return rateLimiter?.isFresh() ?: false
    }

    private val fetchCount = AtomicInt(0)

    init {
        NuBismarckConfig<T>().apply(config).let {
            fetcher = it.fetcher
            rateLimiter = it.rateLimiter
            persister = it.persister ?: persister
        }
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