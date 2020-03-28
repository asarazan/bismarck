package net.sarazan.bismarck.test

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.BismarckState.*
import net.sarazan.bismarck.NuBismarck
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter
import kotlin.test.Test
import kotlin.test.assertEquals

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class JvmTests {

    @Test
    fun testInsert() = runBlocking {
        val bismarck = NuBismarck<String> {
            debug = true
        }
        assertEquals(null, bismarck.value)
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testFreshness() = runBlocking {
        val bismarck = NuBismarck<String> {
            debug = true
            rateLimiter = SimpleRateLimiter(100)
        }
        assertEquals(Stale, bismarck.state)
        bismarck.insert("Foo")
        assertEquals(Fresh, bismarck.state)
        bismarck.invalidate()
        assertEquals(Stale, bismarck.state)
        bismarck.insert("Foo")
        assertEquals(Fresh, bismarck.state)
        delay(200)
        assertEquals(Stale, bismarck.state)
    }

    @Test
    fun testFetch() = runBlocking {
        val bismarck = NuBismarck<String> {
            debug = true
            rateLimiter = SimpleRateLimiter(100)
            fetcher = {
                delay(100)
                "Foo"
            }
        }
        assertEquals(Stale, bismarck.state)
        bismarck.invalidate()
        assertEquals(Fetching, bismarck.state)
        assertEquals(null, bismarck.value)
        delay(50)
        assertEquals(Fetching, bismarck.state)
        assertEquals(null, bismarck.value)
        delay(100)
        assertEquals("Foo", bismarck.value)
        assertEquals(Fresh, bismarck.state)
    }

    @Test
    fun testPersisterInit() = runBlocking {
        val persister = MemoryPersister<String>()
        var bismarck = NuBismarck<String> {
            debug = true
            this.persister = persister
        }
        bismarck.insert("Foo")
        bismarck = NuBismarck {
            debug = true
            this.persister = persister
        }
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testDataChannel() = runBlocking {
        var received: String? = null
        val bismarck = NuBismarck<String> {
            debug = true
        }
        GlobalScope.launch {
            bismarck.valueChannel.consumeEach {
                received = it
            }
        }
        assertEquals(null, received)
        bismarck.insert("Foo")
        delay(100)
        assertEquals("Foo", received)
    }

    @Test
    fun testStateChannel() = runBlocking {
        var received: BismarckState? = null
        val bismarck = NuBismarck<String> {
            debug = true
            rateLimiter = SimpleRateLimiter(100)
            fetcher = {
                delay(100)
                "Foo"
            }
        }
        GlobalScope.launch {
            bismarck.stateChannel.consumeEach {
                received = it
            }
        }
        assertEquals(null, received)
        delay(50)
        assertEquals(Stale, received)
        bismarck.invalidate()
        delay(50)
        assertEquals(Fetching, received)
        delay(100)
        assertEquals(Fresh, received)
    }
}