package net.sarazan.bismarck.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.fetcher
import net.sarazan.bismarck.impl.BaseBismarck
import net.sarazan.bismarck.persisters.FilePersister
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter
import net.sarazan.bismarck.serializers.JavaSerializer
import net.sarazan.bismarck.util.concurrency.SuspendLock
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class JvmTests {

    @Test
    fun testInsert() {
        val bismarck: Bismarck<String> = BaseBismarck<String>()
            .persister(MemoryPersister)
            .rateLimiter(SimpleRateLimiter(15 * 60 * 1000))
            .fetcher {
                // do something expensive here
                "Some hugely important derived value"
            }
        bismarck.eachValue {
            println("Received value $it")
        }

        assertEquals(null, bismarck.peek())
        assertEquals(BismarckState.Stale, bismarck.peekState())
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.peek())
        assertEquals(BismarckState.Fresh, bismarck.peekState())
    }

    @Test
    fun testFetch() = runBlockingTest {
        val lock = SuspendLock(true)

        val bismarck: Bismarck<String> = BaseBismarck<String>().fetcher {
            delay(10)
            "Bar".also {
                lock.unlock()
            }
        }

        bismarck.refresh()
        lock.lock()
        delay(1)

        assertEquals("Bar", bismarck.peek())
        assertEquals(BismarckState.Fresh, bismarck.peekState())
    }

    @Test
    fun testConsume() = runBlockingTest {
        val lock = SuspendLock(true)
        val bismarck: Bismarck<String> = BaseBismarck()
        var value: String? = null
        bismarck.eachValue {
            value = it
            lock.unlock()
        }
        bismarck.insert("Foo")
        lock.lock()
        delay(1)
        assertEquals("Foo", value)
    }

}