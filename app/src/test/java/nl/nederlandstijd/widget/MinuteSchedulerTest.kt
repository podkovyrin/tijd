package nl.nederlandstijd.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class MinuteSchedulerTest {
    @Test
    fun `ticks align with wall clock minutes without accumulating drift`() {
        assertEquals(60_000L, MinuteScheduler.nextMinuteAfter(0))
        assertEquals(60_000L, MinuteScheduler.nextMinuteAfter(59_999))
        assertEquals(120_000L, MinuteScheduler.nextMinuteAfter(60_000))
        assertEquals(180_000L, MinuteScheduler.nextMinuteAfter(128_432))
        assertEquals(86_400_000L, MinuteScheduler.nextMinuteAfter(86_399_999))
    }
}
