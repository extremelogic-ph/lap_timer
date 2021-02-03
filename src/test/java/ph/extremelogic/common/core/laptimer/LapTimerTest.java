package ph.extremelogic.common.core.laptimer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LapTimerTest {
    @Test
    void testSimpleLapTimer() throws InterruptedException {
        var lapTimer = LapTimer.getInstance();
        var key = "simple";
        var delay = 1000L;
        lapTimer.starTimer(key);
        Thread.sleep(delay);
        lapTimer.stopTimer(key);

        assertTrue(lapTimer.getTotalTime(key) > delay);
        assertTrue(lapTimer.getAverageTime(key) > delay);
        assertTrue(lapTimer.getDiffTrackTime(key) > delay);
        assertTrue(lapTimer.getAverageTimePerLap(key) > delay);
        assertEquals(1, lapTimer.getLapCount(key));
    }

    @Test
    void testInLoop() throws InterruptedException {
        var lapTimer = LapTimer.getInstance();
        var key = "loop";
        var delay = 100L;
        var lapCount = 10;

        for (var x = 0; x < lapCount; x++) {
            lapTimer.starTimer(key);
            Thread.sleep(delay);
            lapTimer.stopTimer(key);
            assertEquals(x + 1, lapTimer.getLapCount(key));
        }
        assertTrue(lapTimer.getTotalTime(key) > (delay * lapCount));
        assertEquals(lapCount, lapTimer.getLapCount(key));
    }

    @Test
    void testException() {
        assertThrows(IllegalStateException.class, () -> {LapTimer.getInstance().stopTimer("test"); });
    }
}
