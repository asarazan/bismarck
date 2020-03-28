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
    var scope: CoroutineScope = GlobalScope,
    var debug: Boolean = false
)

@ExperimentalCoroutinesApi
class NuBismarck<T : Any>(config: NuBismarckConfig<T>.() -> Unit = {}) : Closeable {

    private val config = NuBismarckConfig<T>().apply(config)
    val fetcher get() = this.config.fetcher
    val rateLimiter get() = this.config.rateLimiter
    val persister get() = this.config.persister
    val scope get() = this.config.scope

    var value: T?
        get() = trace("value") { persister.get() }
        private set(value) {
            persister.put(value)
            scope.launch {
                valueChannel.send(persister.get())
            }
        }

    val state: BismarckState get() = trace("state") {
        when {
            fetcher != null && fetchCount.get() > 0 -> BismarckState.Fetching
            isFresh -> BismarckState.Fresh
            else -> Stale
        }
    }

    val isFresh: Boolean get() = trace("isFresh") {
        rateLimiter?.isFresh() ?: false
    }

    private val fetchCount = AtomicInt(0)

    val valueChannel by lazy { ConflatedBroadcastChannel(value) }
    val stateChannel by lazy { ConflatedBroadcastChannel(state) }

    init {
        trace("init") {
            scope.launch {
                trace("init in scope") {
                    valueChannel.send(value)
                    stateChannel.send(state)
                }
            }
        }
    }

    fun insert(value: T?) = trace("insert") {
        this.value = value
        rateLimiter?.update()
        updateState()
    }

    fun invalidate() = trace("invalidate") {
        rateLimiter?.reset()
        fetch()
    }

    override fun close() {
        valueChannel.close()
        stateChannel.close()
    }

    private fun fetch() = trace("fetch") {
        if (fetcher == null) return@trace
        fetchCount.incrementAndGet()
        updateState()
        scope.launch {
            trace("fetch in scope") {
                val fetched = fetcher?.invoke()
                insert(fetched)
                fetchCount.decrementAndGet()
                updateState()
            }
        }
    }

    private fun updateState() = trace("updateState") {
        scope.launch {
            trace("updateState in scope") {
                stateChannel.send(state)
            }
        }
    }

    private var indent = 0
    private inline fun log(msg: () -> String) {
        if (this.config.debug) {
            println(buildString {
                (0 until indent).forEach {
                    append(" ")
                }
                append(msg())
            })
        }
    }

    private inline fun <T> trace(msg: String, fn: () -> T): T {
        indent += 1
        log { msg }
        val retval = fn()
//        log { "$msg = $retval" }
        indent -= 1
        return retval
    }
}