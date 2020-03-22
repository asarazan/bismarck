import net.sarazan.bismarck.*
import net.sarazan.bismarck.impl.BaseBismarck
import net.sarazan.bismarck.impl.DedupingBismarck
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter
import net.sarazan.bismarck.serializers.JsonSerializer

internal open class JsBismarck<T : Any>
internal constructor(private val common: BaseBismarck<T>)
    : Bismarck<T> by common
{
    @JsName("Bismarck")
    internal constructor() : this(BaseBismarck<T>())

    override fun peekState(): BismarckState {
        return common.peekState().toString().unsafeCast<BismarckState>()
    }

    /**
     * If you want data to actually, you know, persist; then this is your guy.
     *
     * @default: [MemoryPersister] a simple in-memory persister
     */
    fun persister(persister: Persister<T>?) = apply { common.persister = persister }

    /**
     * Strategy for determining if data is "fresh".
     * Unfresh data will still be presented, but will trigger a fetch.
     *
     * @default: null
     */
    fun rateLimiter(rateLimiter: RateLimiter?) = apply { common.rateLimiter = rateLimiter }
}

@JsName("DedupingBismarck")
internal class JsDedupingBismarck<T : Any>
internal constructor() : JsBismarck<T>(DedupingBismarck<T>())

@ExperimentalStdlibApi
@JsName("jsonSerializer")
internal fun jsonSerializer(): JsonSerializer {
    return JsonSerializer()
}