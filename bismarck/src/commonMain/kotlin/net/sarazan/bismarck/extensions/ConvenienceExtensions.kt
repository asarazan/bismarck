package net.sarazan.bismarck.extensions

import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck

fun <T : Any> Bismarck<T>.postInvalidate() {
    coroutineScope.launch {
        invalidate()
    }
}