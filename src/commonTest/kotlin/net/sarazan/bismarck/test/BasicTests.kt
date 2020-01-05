package net.sarazan.bismarck.test

import net.sarazan.bismarck.Bismarck
import net.sarazan.bismarck.BismarckState
import net.sarazan.bismarck.impl.BaseBismarck
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTests {

    val bismarck: Bismarck<String> = BaseBismarck()

    @BeforeTest
    fun before() {
        bismarck.clear()
    }

    @Test
    fun testInsert() {
        assertEquals(null, bismarck.peek())
        assertEquals(BismarckState.Stale, bismarck.peekState())
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.peek())
        assertEquals(BismarckState.Fresh, bismarck.peekState())
    }

}