package de.veloce.app.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RideRouteMapperTest {
    @Test
    fun `parses latitude longitude pair`() {
        val point = "52.45,8.85".toMapPointOrNull()

        assertEquals(52.45, point?.lat ?: 0.0, 0.0)
        assertEquals(8.85, point?.lng ?: 0.0, 0.0)
    }

    @Test
    fun `rejects invalid coordinates`() {
        assertNull("91,8.85".toMapPointOrNull())
        assertNull("52.45,181".toMapPointOrNull())
        assertNull("NaN,8.85".toMapPointOrNull())
        assertNull("0,0".toMapPointOrNull())
    }
}
