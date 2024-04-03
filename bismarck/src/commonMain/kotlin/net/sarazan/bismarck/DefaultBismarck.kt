package net.sarazan.bismarck

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck.State
import net.sarazan.bismarck.Bismarck.State.Stale
import kotlin.time.ComparableTimeMark

class DefaultBismarck<T : Any>(private val config: Bismarck.Config<T>) : Bismarck<T> {

    private val _fetchCount = AtomicInt(0)
    private var fetchJob: Job? = null
    private var freshnessJob: Job? = null

    private val fetcher get() = config.fetcher
    private val freshness get() = config.freshness
    private val storage get() = config.storage
    private val dispatcher get() = config.dispatcher

    override val coroutineScope: CoroutineScope
        get() = CoroutineScope(dispatcher + SupervisorJob())

    override val values: StateFlow<T?>
        get() = _values.also {
            coroutineScope.launch { check() }
        }
    private val _values = MutableStateFlow(storage.get())

    override val states: StateFlow<State?>
        get() = _states.also {
            coroutineScope.launch { check() }
        }
    private val _states = MutableStateFlow(getState())

    override val errors: StateFlow<Throwable?>
        get() = _errors.also {
            coroutineScope.launch { check() }
        }
    private val _errors = MutableStateFlow<Throwable?>(null)

    init {
        coroutineScope.launch {
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
        insert(value, false)
    }

    override suspend fun invalidate() {
        resetFreshness()
        check()
    }

    override suspend fun clear() {
        insert(null, true)
    }

    private suspend fun fetch() {
        val fetch = fetcher ?: return
        val timeMark = config.timeSource.markNow()
        _fetchCount.incrementAndGet()
        updateState()
        val job = fetchJob
        fetchJob = coroutineScope.launch {
            // join call acts as a single-pass gate for deduping requests
            job?.join()
            try {
                if (freshness?.isFresh() != true) {
                    insert(fetch.invoke(), false, timeMark)
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

    private suspend fun insert(
        value: T?,
        reset: Boolean,
        timeMark: ComparableTimeMark = config.timeSource.markNow()
    ) {
        _values.emit(value)
        storage.put(value)
        if (reset) {
            resetFreshness()
        } else {
            updateFreshness(timeMark)
        }
    }

    private suspend fun resetFreshness() {
        freshness?.reset()
        updateState()
    }

    private suspend fun updateFreshness(timeMark: ComparableTimeMark) {
        freshness?.update(timeMark)
        updateState()
        freshness?.remainingTime()?.let {
            freshnessJob?.cancel()
            freshnessJob = coroutineScope.launch {
                delay(it)
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
