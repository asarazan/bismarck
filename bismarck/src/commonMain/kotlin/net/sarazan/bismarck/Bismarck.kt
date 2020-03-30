package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import net.sarazan.bismarck.BismarckState.Stale
import net.sarazan.bismarck.platform.Closeable
import net.sarazan.bismarck.platform.currentTimeNano

@ExperimentalCoroutinesApi
class Bismarck<T : Any>(private val config: BismarckConfig<T>) : Closeable {
    constructor(config: BismarckConfig<T>.() -> Unit = {}) : this(BismarckConfig<T>().apply(config))

    val fetcher get() = this.config.fetch
    val rateLimiter get() = this.config.rateLimiter
    val storage get() = this.config.storage
    val scope get() = this.config.scope

    var value: T?
        get() = trace("value") { storage.get() }
        private set(value) = trace("setValue $value") {
            storage.put(value)
            scope.launch {
                valueChannel.send(storage.get())
            }
        }

    val state: BismarckState get() = when {
        fetchCount > 0 -> BismarckState.Fetching
        isFresh -> BismarckState.Fresh
        else -> Stale
    }.apply {
        log { "getState: $this" }
    }

    var error: Exception? = null
        get() = field.apply {
            log { "getException: $this" }
        }
        private set(value) {
            field = value
            scope.launch {
                errorChannel.send(field)
            }
        }

    val isFresh: Boolean get() = trace("isFresh") {
        rateLimiter?.isFresh() ?: false
    }

    val fetchCount get() = _fetchCount.get()
    private val _fetchCount = AtomicInt(0)
    private var fetchJob: Job? = null

    val valueChannel by lazy { ConflatedBroadcastChannel(value) }
    val stateChannel by lazy { ConflatedBroadcastChannel(state) }
    val errorChannel by lazy { ConflatedBroadcastChannel(error) }

    init {
        trace("init") {
            scope.launch {
                trace("init in scope") {
                    valueChannel.send(value)
                    stateChannel.send(state)
                    errorChannel.send(error)
                }
            }
        }
    }

    fun insert(value: T?) {
        insert(value, currentTimeNano())
    }

    fun invalidate() = trace("invalidate") {
        rateLimiter?.reset()
        fetch()
    }

    override fun close() {
        valueChannel.close()
        stateChannel.close()
        errorChannel.close()
    }

    internal fun fetch() = trace("fetch") {
        val fetch = fetcher ?: return@trace
        val time = currentTimeNano()
        _fetchCount.incrementAndGet()
        updateState()
        val job = fetchJob
        fetchJob = scope.launch {
            job?.join()
            try {
                if (rateLimiter?.isFresh() != true) {
                    insert(fetch.invoke(), time)
                    error = null
                }
            } catch (e: Exception) {
                log { "Received error: $e" }
                error = e
            } finally {
                _fetchCount.decrementAndGet()
                updateState()
            }
        }
    }

    private fun insert(value: T?, timestamp: Long) = trace("insert $value ($timestamp)") {
        this.value = value
        rateLimiter?.update(timestamp)
        updateState()
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
