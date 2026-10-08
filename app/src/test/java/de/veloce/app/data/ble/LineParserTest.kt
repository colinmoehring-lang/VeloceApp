package de.veloce.app.data.ble

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LineParserTest {
    private val receivedAt = Instant.parse("2026-10-08T09:00:00Z")

    @Test
    fun `parses a complete line and uses app receive time`() {
        val result = parser().feed("42.3,-18.5,1.12,51.123456,8.123456\n".toByteArray()).single()

        assertTrue(result.isValid)
        assertEquals(receivedAt, result.point?.timestamp)
        assertEquals(42.3, result.point?.speedKmh ?: 0.0, 0.0)
    }

    @Test
    fun `reassembles a line delivered in three packets`() {
        val parser = parser()
        val first = "42.3,-".toByteArray()
        val second = "18.5,1.12,51.".toByteArray()
        val third = "123456,8.123456\n".toByteArray()

        assertTrue(parser.feed(first).isEmpty())
        assertTrue(parser.feed(second).isEmpty())
        val result = parser.feed(third).single()

        assertTrue(result.isValid)
        assertEquals(-18.5, result.point?.leanDegrees ?: 0.0, 0.0)
    }

    @Test
    fun `accepts windows line ending`() {
        val result = parser().feed("1,2,3,4,5\r\n".toByteArray()).single()

        assertEquals("1,2,3,4,5", result.rawLine)
        assertTrue(result.isValid)
    }

    @Test
    fun `parses two lines in one packet`() {
        val result = parser().feed("1,2,3,4,5\n6,7,8,9,10\n".toByteArray())

        assertEquals(2, result.size)
        assertEquals(6.0, result[1].point?.speedKmh ?: 0.0, 0.0)
    }

    @Test
    fun `discards partial initial line after connection`() {
        val parser = parser().apply { reset(discardUntilFirstNewline = true) }

        val result = parser.feed("ed,0,0,0,0\n42.3,-18.5,1.12,51.1,8.1\n".toByteArray())

        assertEquals(2, result.size)
        assertFalse(result.first().isValid)
        assertTrue(result.first().rawLine.contains("ed,0,0"))
        assertTrue(result.last().isValid)
        assertTrue(result.last().rawLine.startsWith("42.3"))
    }

    @Test
    fun `marks line with wrong number of fields invalid`() {
        val result = parser().feed("1,2,3,4\n".toByteArray()).single()

        assertFalse(result.isValid)
        assertNull(result.point)
    }

    @Test
    fun `marks alphabetic measurement invalid`() {
        val result = parser().feed("fast,2,3,4,5\n".toByteArray()).single()

        assertFalse(result.isValid)
    }

    @Test
    fun `preserves negative values`() {
        val result = parser().feed("-2.5,-18.5,-1.12,51.1,8.1\n".toByteArray()).single()

        assertEquals(-2.5, result.point?.speedKmh ?: 0.0, 0.0)
        assertEquals(-18.5, result.point?.leanDegrees ?: 0.0, 0.0)
        assertEquals(-1.12, result.point?.gForce ?: 0.0, 0.0)
    }

    @Test
    fun `does not parse comma decimal separator`() {
        val result = parser().feed("42,3,-18.5,1.12,51.1,8.1\n".toByteArray()).single()

        assertFalse(result.isValid)
    }

    @Test
    fun `marks an unfinished line invalid when the stream ends`() {
        val parser = parser()
        assertTrue(parser.feed("42.3,-18.5,1.12".toByteArray()).isEmpty())

        val result = parser.finish()

        assertFalse(result?.isValid ?: true)
        assertEquals("42.3,-18.5,1.12", result?.rawLine)
    }

    @Test
    fun `marks an empty line invalid`() {
        val result = parser().feed("\n".toByteArray()).single()

        assertFalse(result.isValid)
        assertEquals("", result.rawLine)
    }

    private fun parser() = LineParser(clock = { receivedAt })
}
