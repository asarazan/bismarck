package net.sarazan.bismarck.mobile

import kotlin.native.concurrent.ThreadLocal
import kotlinx.coroutines.*
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.platform.BismarckDispatchers
import net.sarazan.bismarck.ratelimit.SimpleFreshness

class FooViewModel(private val scope: CoroutineScope) {

    constructor() : this(CoroutineScope(BismarckDispatchers.main))

    @ThreadLocal
    companion object {
        private var counter = 0
        val bismarck = Bismarck.create<String> {
            checkOnLaunch = true
            freshness = SimpleFreshness(60 * 1000)
        }
    }

    val foo = bismarck.rescope(scope).apply {
        println("Created FooViewModel")
        scope.launch {
            while (true) {
                delay(1000)
                insert("${counter++}")
            }
        }
    }

    fun tearDown() {
        println("Tearing Down")
        scope.cancel()
    }
}
