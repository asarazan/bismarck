package net.sarazan.bismarck.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.*
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.BismarckState.*
import net.sarazan.bismarck.ratelimit.SimpleFreshness
import net.sarazan.bismarck.storage.MemoryStorage

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CommonTests {

    @Test
    fun testInsert() = runBlockingTest {
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            }
        assertEquals(null, bismarck.value)
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testFreshness() = runBlockingTest {
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            freshness = SimpleFreshness(100)
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
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            freshness = SimpleFreshness(100)
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
    fun testPersisterInit() = runBlockingTest {
        val persister = MemoryStorage<String>()
        var bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            this.storage = persister
        }
        bismarck.insert("Foo")
        bismarck = Bismarck.create {
            logDebug = shouldDebug
            this.storage = persister
        }
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testDataChannel() = runBlockingTest {
        var received: String? = null
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            }
        GlobalScope.launch {
            bismarck.eachValue {
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
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            freshness = SimpleFreshness(100)
            fetcher = {
                delay(100)
                "Foo"
            }
        }
        GlobalScope.launch {
            bismarck.eachState {
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
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            fetcher = {
                delay(100)
                throw exception
            }
        }
        GlobalScope.launch {
            bismarck.eachError {
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

    @Test
    fun testDedupe() = runBlockingTest {
        var counter = 0
        val bismarck = Bismarck.create<String> {
            logDebug = shouldDebug
            freshness = SimpleFreshness(1000)
            fetcher = {
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
