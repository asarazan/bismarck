package net.sarazan.bismarck.test

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.Bismarck.State
import net.sarazan.bismarck.Bismarck.State.Fetching
import net.sarazan.bismarck.Bismarck.State.Fresh
import net.sarazan.bismarck.Bismarck.State.Stale
import net.sarazan.bismarck.freshness.SimpleFreshness
import net.sarazan.bismarck.storage.MemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CommonTests {

    @Test
    fun testInsert() = runBlockingTest {
        val bismarck = Bismarck.create<String>()
        assertEquals(null, bismarck.value)
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testFreshness() = runBlockingTest {
        val bismarck = Bismarck.create<String> {
            freshness = SimpleFreshness(100)
        }
        assertEquals(Stale, bismarck.states.value)
        bismarck.insert("Foo")
        assertEquals(Fresh, bismarck.states.value)
        bismarck.invalidate()
        assertEquals(Stale, bismarck.states.value)
        bismarck.insert("Foo")
        assertEquals(Fresh, bismarck.states.value)
        delay(200)
        assertEquals(Stale, bismarck.states.value)
    }

    @Test
    fun testFetch() = runBlockingTest {
        val bismarck = Bismarck.create<String> {
            freshness = SimpleFreshness(100)
            fetcher = {
                delay(100)
                "Foo"
            }
        }
        assertEquals(Stale, bismarck.states.value)
        bismarck.invalidate()
        assertEquals(Fetching, bismarck.states.value)
        assertEquals(null, bismarck.value)
        delay(50)
        assertEquals(Fetching, bismarck.states.value)
        assertEquals(null, bismarck.value)
        delay(100)
        assertEquals("Foo", bismarck.value)
        assertEquals(Fresh, bismarck.states.value)
    }

    @Test
    fun testPersisterInit() = runBlockingTest {
        val persister = MemoryStorage<String>()
        var bismarck = Bismarck.create<String> {
            this.storage = persister
        }
        bismarck.insert("Foo")
        bismarck = Bismarck.create {

            this.storage = persister
        }
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testDataChannel() = runBlockingTest {
        var received: String? = null
        val bismarck = Bismarck.create<String>()
        GlobalScope.launch {
            bismarck.values.collectLatest {
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
        var received: State? = null
        val bismarck = Bismarck.create<String> {
            freshness = SimpleFreshness(100)
            fetcher = {
                delay(100)
                "Foo"
            }
        }
        GlobalScope.launch {
            bismarck.states.collectLatest {
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
        var exception: Exception? = RuntimeException("Foo")
        var received: Throwable? = null
        val bismarck = Bismarck.create<String> {
            fetcher = {
                delay(100)
                exception?.let { throw it }
            }
        }
        GlobalScope.launch {
            bismarck.errors.collect {
                received = it
            }
        }
        assertEquals(null, bismarck.errors.value)
        assertEquals(null, received)
        bismarck.invalidate()
        assertEquals(null, bismarck.errors.value)
        assertEquals(null, received)
        delay(200)
        assertEquals(exception, bismarck.errors.value)
        assertEquals(exception, received)
        exception = null
        bismarck.check()
        delay(200)
        assertEquals(null, bismarck.errors.value)
        assertEquals(null, received)
    }

    @Test
    fun testDedupe() = runBlockingTest {
        var counter = 0

        // Simple bismarck that is fresh for 1s, fetches in 100ms, and increments each time.
        val bismarck = Bismarck.create<String> {
            freshness = SimpleFreshness(1000)
            fetcher = {
                delay(100)
                "Foob-${counter++}"
            }
        }

        GlobalScope.launch {
            bismarck.states.collectLatest {
                println("Dedupe State: $it")
            }

            bismarck.values.collectLatest {
                println("Dedupe Value: $it")
            }
        }

        println("Should be null and stale. Trigger an invalidate")
        assertEquals(Stale, bismarck.states.value)
        bismarck.invalidate()

        println("Should be null and fetching because it takes 100ms.")
        assertEquals(Fetching, bismarck.states.value)
        println("delay 150")
        delay(150)

        println("Should be fresh and in position 0. Invalidate again.")
        assertEquals(Fresh, bismarck.states.value)
        assertEquals("Foob-0", bismarck.value)
        bismarck.invalidate()

        println("Should be fetching and in position 0 for at least 90ms.")
        assertEquals(Fetching, bismarck.states.value)
        println("delay 90")
        delay(90)
        assertEquals(Fetching, bismarck.states.value)
        assertEquals("Foob-0", bismarck.value)

        println("Trigger a new invalidate. This should theoretically queue up a second fetch in about 10ms")
        bismarck.invalidate()
        println("delay 50")
        delay(50)

        println("We should be on the second fetch by now. Current position is 1")
        assertEquals(Fetching, bismarck.states.value)
        assertEquals("Foob-1", bismarck.value)
        println("delay 100")
        delay(100)

        println("The final fetch came in. Fresh in position 2")
        assertEquals(Fresh, bismarck.states.value)
        assertEquals("Foob-2", bismarck.value)
    }
}
