package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck.State
import net.sarazan.bismarck.Bismarck.State.Stale
import net.sarazan.bismarck.platform.currentTimeNano
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class DefaultBismarck<T : Any>(private val config: Bismarck.Config<T>) : Bismarck<T> {

    override var value: T?
        get() = storage.get()
        set(value) {
            storage.put(value)
            scope.launch {
                _values.emit(storage.get())
            }
        }

    private val _fetchCount = AtomicInt(0)
    private var fetchJob: Job? = null
    private var freshnessJob: Job? = null

    private val fetcher get() = config.fetcher
    private val freshness get() = config.freshness
    private val storage get() = config.storage
    private val dispatcher get() = config.dispatcher
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    override val values: StateFlow<T?>
        get() = _values
    private val _values = MutableStateFlow(value)

    override val states: StateFlow<State?>
        get() = _states
    private val _states = MutableStateFlow(getState())

    override val errors: StateFlow<Throwable?>
        get() = _errors
    private val _errors = MutableStateFlow<Throwable?>(null)

    init {
        scope.launch {
            if (config.checkOnLaunch) {
                check()
            }
        }
    }

    override suspend fun check() {
        if (freshness?.isFresh() != true) {
            fetch()
        }
    }

    override suspend fun insert(value: T?) {
        insert(value, currentTimeNano(), false)
    }

    override suspend fun invalidate() {
        resetFreshness()
        check()
    }

    override suspend fun clear() {
        insert(null, currentTimeNano(), true)
    }

    private suspend fun fetch() {
        val fetch = fetcher ?: return
        val time = currentTimeNano()
        _fetchCount.incrementAndGet()
        updateState()
        val job = fetchJob
        fetchJob = scope.launch {
            // join call acts as a single-pass gate for deduping requests
            job?.join()
            try {
                if (freshness?.isFresh() != true) {
                    insert(fetch.invoke(), time, false)
                    _errors.emit(null)
                }
            } catch (e: Exception) {
                _errors.emit(e)
                resetFreshness()
            } finally {
                _fetchCount.decrementAndGet()
                updateState()
            }
        }
    }

    private suspend fun insert(value: T?, timestamp: Long, reset: Boolean) {
        this.value = value
        if (reset) {
            resetFreshness()
        } else {
            updateFreshness(timestamp)
        }
    }

    private suspend fun resetFreshness() {
        freshness?.reset()
        updateState()
    }

    private suspend fun updateFreshness(timestamp: Long) {
        freshness?.update(timestamp)
        updateState()
        freshness?.getRemainingNanos()?.let {
            freshnessJob?.cancel()
            freshnessJob = scope.launch {
                delay(it.nanoseconds)
                updateState()
            }
        }
    }

    private suspend fun updateState() {
        _states.emit(getState())
    }

    private fun getState() = when {
        _fetchCount.get() > 0 -> State.Fetching
        freshness?.isFresh() ?: false -> State.Fresh
        else -> Stale
    }
}
