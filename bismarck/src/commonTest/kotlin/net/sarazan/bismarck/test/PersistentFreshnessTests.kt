package net.sarazan.bismarck.test

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.Bismarck.State.Fresh
import net.sarazan.bismarck.Bismarck.State.Stale
import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.freshness.PersistentFreshness
import net.sarazan.bismarck.serialization.StringSerializer
import net.sarazan.bismarck.storage.FileStorage

class PersistentFreshnessTests {

    @BeforeTest
    fun setup() {
        teardown()
    }

    @AfterTest
    fun teardown() {
        File("./foo.txt").delete()
        File("./storage.txt").delete()
    }

    @Test
    fun testPersistentFreshness() = runBlockingTest {
        var bismarck = Bismarck.create {
            storage = FileStorage("./storage.txt", StringSerializer)
            freshness = PersistentFreshness("./foo.txt", 1000)
        }
        assertEquals(bismarck.states.value, Stale)
        bismarck.insert("foo")
        assertEquals(bismarck.value, "foo")
        assertEquals(bismarck.states.value, Fresh)

        bismarck = Bismarck.create {
            storage = FileStorage("./storage.txt", StringSerializer)
            freshness = PersistentFreshness("./foo.txt", 1000)
        }
        assertEquals(bismarck.value, "foo")
        assertEquals(bismarck.states.value, Fresh)
    }
}
