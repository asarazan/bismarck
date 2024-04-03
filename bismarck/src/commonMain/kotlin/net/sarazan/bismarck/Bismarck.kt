package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import net.sarazan.bismarck.extensions.FreshnessConfig
import net.sarazan.bismarck.freshness.Freshness
import net.sarazan.bismarck.freshness.PersistentFreshness
import net.sarazan.bismarck.freshness.SimpleFreshness
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
        var storage: Storage<T> = MemoryStorage(),
        var dispatcher: CoroutineDispatcher = BismarckDispatchers.default,
        var checkOnLaunch: Boolean = false,
        var timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic
    ) {
        // Bismarck and Freshness must share a TimeSource instance, so this can only be configured
        // with the builder function below.
        var freshness: Freshness? = null
            private set

        fun freshness(init: FreshnessConfig.() -> Unit) {
            val fresh = FreshnessConfig().apply(init)
            freshness = when (val it = fresh.path) {
                null -> SimpleFreshness(fresh.duration, timeSource)
                else -> PersistentFreshness(it, fresh.duration, timeSource)
            }
        }
    }


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
