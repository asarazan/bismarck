package net.sarazan.bismarck.test

import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.platform.readString
import net.sarazan.bismarck.serialization.StringSerializer
import net.sarazan.bismarck.storage.FileStorage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FileStorageTests {

    val file = "./foo.txt"

    @BeforeTest
    fun setup() {
        File(file).delete()
    }

    @Test
    fun testFileValue() = runBlockingTest {
        var bismarck = Bismarck.create {
            storage = FileStorage(file, StringSerializer)
        }
        assertEquals(bismarck.values.value, null)

        bismarck.insert("foob")
        assertEquals(bismarck.values.value, "foob")
        assertEquals(File(file).readString(), "foob")

        bismarck = Bismarck.create {
            storage = FileStorage(file, StringSerializer)
        }
        assertEquals(bismarck.values.value, "foob")

        bismarck.clear()
        assertFalse(File(file).exists)
    }
}
