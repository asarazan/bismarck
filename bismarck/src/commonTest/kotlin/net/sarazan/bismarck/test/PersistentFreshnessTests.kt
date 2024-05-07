package net.sarazan.bismarck.test

import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.Bismarck.State.Fresh
import net.sarazan.bismarck.Bismarck.State.Stale
import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.serialization.StringSerializer
import net.sarazan.bismarck.storage.FileStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

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
            freshness {
                path = "./foo.txt"
                duration = 100.milliseconds
            }
        }
        assertEquals(bismarck.states.value, Stale)
        bismarck.insert("foo")
        assertEquals(bismarck.values.value, "foo")
        assertEquals(bismarck.states.value, Fresh)

        bismarck = Bismarck.create {
            storage = FileStorage("./storage.txt", StringSerializer)
            freshness {
                path = "./foo.txt"
                duration = 1000.milliseconds
            }
        }
        assertEquals(bismarck.values.value, "foo")
        assertEquals(bismarck.states.value, Fresh)
    }
}
