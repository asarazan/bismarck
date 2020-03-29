package net.sarazan.bismarck.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckState.*
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class DedupeTests {

    @Test
    fun foo() = runBlockingTest {
        var counter = 0
        val bismarck = Bismarck<String> {
            debug = shouldDebug
            rateLimiter = SimpleRateLimiter(1000)
            fetch = {
                delay(100)
                "Foob-${counter++}"
            }
        }
        assertEquals(Stale, bismarck.state)
        bismarck.invalidate()
        assertEquals(Fetching, bismarck.state)
        delay(150)
        assertEquals(Fresh, bismarck.state)
        assertEquals("Foob-0", bismarck.value)
        bismarck.invalidate()
        assertEquals(Fetching, bismarck.state)
        delay(90)
        assertEquals(Fetching, bismarck.state)
        assertEquals("Foob-0", bismarck.value)
        bismarck.invalidate()
        delay(50)
        assertEquals(Fetching, bismarck.state)
        assertEquals("Foob-1", bismarck.value)
        delay(100)
        assertEquals(Fresh, bismarck.state)
        assertEquals("Foob-2", bismarck.value)
    }
}
