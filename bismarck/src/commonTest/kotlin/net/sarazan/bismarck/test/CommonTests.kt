package net.sarazan.bismarck.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.BismarckState.*
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter

val debugLogs = false
val shouldDedupe = false

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CommonTests {

    @Test
    fun testInsert() = runBlockingTest {
        val bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
        }
        assertEquals(null, bismarck.value)
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testFreshness() = runBlockingTest {
        val bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
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
    fun testFetch() = runBlockingTest {
        val bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
            rateLimiter = SimpleRateLimiter(100)
            fetch = {
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
    fun testPersisterInit() = runBlockingTest {
        val persister = MemoryPersister<String>()
        var bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
            this.persister = persister
        }
        bismarck.insert("Foo")
        bismarck = Bismarck {
            debug = debugLogs
            dedupe = shouldDedupe
            this.persister = persister
        }
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testDataChannel() = runBlockingTest {
        var received: String? = null
        val bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
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
    fun testStateChannel() = runBlockingTest {
        var received: BismarckState? = null
        val bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
            rateLimiter = SimpleRateLimiter(100)
            fetch = {
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

    @Test
    fun testError() = runBlockingTest {
        val exception = RuntimeException("Foo")
        var received: Exception? = null
        val bismarck = Bismarck<String> {
            debug = debugLogs
            dedupe = shouldDedupe
            fetch = {
                delay(100)
                throw exception
            }
        }
        GlobalScope.launch {
            bismarck.errorChannel.consumeEach {
                received = it
            }
        }
        assertEquals(null, bismarck.error)
        assertEquals(null, received)
        bismarck.invalidate()
        assertEquals(null, bismarck.error)
        assertEquals(null, received)
        delay(200)
        assertEquals(exception, bismarck.error)
        assertEquals(exception, received)
    }
}
