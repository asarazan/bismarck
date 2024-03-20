package net.sarazan.bismarck.test

import kotlin.test.*
import net.sarazan.bismarck.platform.File
import net.sarazan.bismarck.platform.readString
import net.sarazan.bismarck.platform.writeString

class FileTests {

    @BeforeTest
    fun setup() {
        File("./foo.txt").delete()
    }

    @Test
    @ExperimentalStdlibApi
    fun writeAndReadFile() = runBlockingTest {
        var file = File("./foo.txt")
        assertFalse(file.exists)

        val msg = "foobar"
        file.writeString(msg)
        assertTrue(file.exists)

        file = File("./foo.txt")
        assertTrue(file.exists)
        val read = file.readString()
        assertEquals(read, msg)

        file.delete()
        assertFalse(file.exists)
    }
}
