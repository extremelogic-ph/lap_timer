package ph.extremelogic.common.core.laptimer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LapTimerTest {
    @Test
    void testSimpleLapTimer() throws InterruptedException {
        var lapTimer = LapTimer.getInstance();
        var tag = "simple";
        var delay = 1000L;
        lapTimer.starTimer(tag);
        assertTrue(lapTimer.isStarted(tag));
        assertFalse(lapTimer.isStopped(tag));
        Thread.sleep(delay);
        lapTimer.stopTimer(tag);
        assertFalse(lapTimer.isStarted(tag));
        assertTrue(lapTimer.isStopped(tag));

        assertTrue(lapTimer.getTotalTime(tag) > delay);
        assertTrue(lapTimer.getAverageTime(tag) > delay);
        assertTrue(lapTimer.getDiffTrackTime(tag) > delay);
        assertTrue(lapTimer.getAverageTimePerLap(tag) > delay);
        assertEquals(1, lapTimer.getLapCount(tag));
    }

    @Test
    void testInLoop() throws InterruptedException {
        var lapTimer = LapTimer.getInstance();
        var tag = "loop";
        var delay = 100L;
        var lapCount = 10;
        var tagInner = "inner";

        for (var x = 0; x < lapCount; x++) {
            lapTimer.starTimer(tag);
            assertTrue(lapTimer.isStarted(tag));
            assertFalse(lapTimer.isStopped(tag));
            Thread.sleep(delay);
            for (var y = 0; y < lapCount; y++) {
                lapTimer.starTimer(tagInner);
                assertTrue(lapTimer.isStarted(tagInner));
                assertFalse(lapTimer.isStopped(tagInner));
                Thread.sleep(delay);
                lapTimer.stopTimer(tagInner);
                assertFalse(lapTimer.isStarted(tagInner));
                assertTrue(lapTimer.isStopped(tagInner));
            }
            lapTimer.stopTimer(tag);
            assertFalse(lapTimer.isStarted(tag));
            assertTrue(lapTimer.isStopped(tag));
            assertEquals(x + 1, lapTimer.getLapCount(tag));
        }
        assertTrue(lapTimer.getTotalTime(tag) > (delay * lapCount) * 2);
        assertTrue(lapTimer.getTotalTime(tagInner) > (delay * lapCount * lapCount));
        assertEquals(lapCount, lapTimer.getLapCount(tag));
    }

    @Test
    void testException() {
        assertThrows(IllegalStateException.class, () -> {LapTimer.getInstance().stopTimer("test"); });
    }
}
