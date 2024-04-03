package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import net.sarazan.bismarck.freshness.Freshness
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.storage.MemoryStorage
import net.sarazan.bismarck.storage.Storage
import kotlin.time.TimeSource

@DslMarker
annotation class BismarckConfigMarker

typealias Fetcher<T> = suspend () -> T?

interface Bismarck<T : Any> {

    @BismarckConfigMarker
    data class Config<T : Any>(
        var fetcher: Fetcher<T>? = null,
        var freshness: Freshness? = null,
        var storage: Storage<T> = MemoryStorage(),
        var dispatcher: CoroutineDispatcher = BismarckDispatchers.default,
        var checkOnLaunch: Boolean = false,
        var timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic
    )

    enum class State {
        Fresh,
        Stale,
        Fetching
    }

    val values: StateFlow<T?>
    val states: StateFlow<State?>
    val errors: StateFlow<Throwable?>

    val coroutineScope: CoroutineScope

    suspend fun insert(value: T?)
    suspend fun invalidate()
    suspend fun check()
    suspend fun clear()

    companion object {
        fun <T : Any> create(config: Config<T>): Bismarck<T> {
            return DefaultBismarck(config)
        }
        fun <T : Any> create(config: Config<T>.() -> Unit = {}): Bismarck<T> {
            return create(Config<T>().apply(config))
        }
    }
}
