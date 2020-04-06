package net.sarazan.bismarck

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RescopedBismarck<T : Any>(
    private val parent: Bismarck<T>,
    private val scope: CoroutineScope
) : Bismarck<T> by parent {

    fun onValue(fn: (T?) -> Unit) {
        scope.launch {
            parent.eachValue(fn)
        }
    }
    fun onState(fn: (Bismarck.State?) -> Unit) {
        scope.launch {
            parent.eachState(fn)
        }
    }
    fun onError(fn: (Exception?) -> Unit) {
        scope.launch {
            parent.eachError(fn)
        }
    }
}
