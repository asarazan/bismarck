package net.sarazan.bismarck.test

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.platform.readString
import net.sarazan.bismarck.serialization.StringSerializer
import net.sarazan.bismarck.storage.FileStorage

class FileStorageTests {

    val file = "./foo.txt"

    @BeforeTest
    fun setup() {
        File(file).delete()
    }

    @Test
    fun testFileValue() {
        var bismarck = Bismarck.create<String> {
            storage = FileStorage(file, StringSerializer())
        }
        assertEquals(bismarck.value, null)

        bismarck.insert("foob")
        assertEquals(bismarck.value, "foob")
        assertEquals(File(file).readString(), "foob")

        bismarck = Bismarck.create {
            storage = FileStorage(file, StringSerializer())
        }
        assertEquals(bismarck.value, "foob")

        bismarck.clear()
        assertFalse(File(file).exists)
    }
}
