import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.impl.BaseBismarck
import net.sarazan.bismarck.impl.DedupingBismarck
import net.sarazan.bismarck.serializers.JsonSerializer

internal open class JsBismarck<T : Any>
internal constructor(private val common: Bismarck<T>)
    : Bismarck<T> by common
{
    @JsName("Bismarck")
    internal constructor() : this(BaseBismarck<T>())

    override fun peekState(): BismarckState {
        return common.peekState().toString().unsafeCast<BismarckState>()
    }
}

@JsName("DedupingBismarck")
internal class JsDedupingBismarck<T : Any>
internal constructor() : JsBismarck<T>(DedupingBismarck<T>())

@ExperimentalStdlibApi
@JsName("jsonSerializer")
internal fun jsonSerializer(): JsonSerializer {
    return JsonSerializer()
}