/*
 MIT License

 Copyright (c) 2021 Virgilio So

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package ph.extremelogic.common.core.laptimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public final class LapTimer {
    /**
     * Time hour constant.
     */
    private static final int TIME_HOUR = 24;

    /**
     * Time minutes constant.
     */
    private static final int TIME_MINS = 60;

    /**
     * Time seconds constant.
     */
    private static final int TIME_SECS = 60;

    /**
     * Time millis constant.
     */
    private static final int TIME_MILLIS = 1000;

    /**
     * State of the timer.
     */
    private enum State {
        /**
         * Initial state.
         */
        UNSTARTED {
            @Override
            boolean isStarted() {
                return false;
            }

            @Override
            boolean isStopped() {
                return true;
            }
        },
        /**
         * Running state.
         */
        RUNNING {
            @Override
            boolean isStarted() {
                return true;
            }

            @Override
            boolean isStopped() {
                return false;
            }
        },
        /**
         * Stopped state.
         */
        STOPPED {
            @Override
            boolean isStarted() {
                return false;
            }

            @Override
            boolean isStopped() {
                return true;
            }
        };

        abstract boolean isStarted();

        abstract boolean isStopped();
    }

    /**
     * Lap tracker object for tags.
     */
    private final Map<String, LapTracker> lapTracker;

    /**
     * Instance object for this class.
     */
    private static LapTimer instance;

    /**
     * Timer stage.
     */
    private LapTimer.State timerState = LapTimer.State.UNSTARTED;

    /**
     * Get the object instance.
     *
     * @return Instance of this class.
     */
    public static LapTimer getInstance() {
        if (instance == null) {
            instance = new LapTimer();
        }
        return instance;
    }

    private LapTimer() {
        lapTracker = new HashMap<>();
    }

    /**
     * Reset time for tag.
     *
     * @param tag Timer tag name.
     */
    public void resetTrackTime(final String tag) {
        if (this.timerState == State.RUNNING) {
            if (lapTracker.containsKey(tag)) {
                lapTracker.remove(tag);
            }
        } else {
            throw new IllegalStateException("Timer is not running.");
        }
    }

    /**
     * Formats millis.
     *
     * @param millis Millisecond value to be formatted.
     * @return Formatted.
     */
    public String formatMillis(final long millis) {
        var sb = new StringBuilder();
        var seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        var minutes = seconds / TIME_SECS;
        var hours = minutes / TIME_MINS;
        var days = hours / TIME_HOUR;
        sb.append(
                format("%02d:", days)
                        + format("%02d:", hours % TIME_HOUR)
                        + format("%02d:", minutes % TIME_MINS)
                        + format("%02d.", seconds % TIME_SECS)
                        + format("%03d", millis % TIME_MILLIS)
        );
        return sb.toString();
    }

    /**
     * Determines if time is started.
     *
     * @return true if started.
     */
    public boolean isStarted() {
        return timerState.isStarted();
    }

    /**
     * Determines if the time is stopped.
     *
     * @return true if stopped.
     */
    public boolean isStopped() {
        return timerState.isStopped();
    }

    /**
     * Start the timer.
     *
     * @param tag Timer tag.
     * @return Time when started.
     */
    public long starTimer(final String tag) {
        if (this.timerState == State.RUNNING) {
            throw new IllegalStateException("Timer already started.");
        }
        var currentTime = System.currentTimeMillis();
        var lapCount = 0;
        LapTracker lap;
        if (lapTracker.containsKey(tag)) {
            lap = lapTracker.get(tag);
            lapCount = lap.getLapTime().getLap();
        } else {
            lap = new LapTracker();
        }
        lapCount++;
        var run = new RunTracker();
        run.setStartTime(currentTime);
        run.setLap(lapCount);
        lap.setLapTime(run);
        lapTracker.put(tag, lap);

        this.timerState = LapTimer.State.RUNNING;
        return currentTime;
    }

    /**
     * Stops the timer.
     *
     * @param tag Timer tag.
     * @return time when stopped.
     */
    public long stopTimer(final String tag) {
        var currentTime = 0L;
        if (this.timerState != LapTimer.State.RUNNING) {
            throw new IllegalStateException("Timer is not running. ");
        }
        currentTime = System.currentTimeMillis();
        var lap = lapTracker.get(tag);
        var runTracker = lap.getLapTime();
        runTracker.setStopTime(currentTime);
        lap.setLapTime(runTracker);
        var totalTime = lap.getTotalTime()
                + (lap.getLapTime().getTimeDiff());
        lap.setTotalTime(totalTime);
        var runTrackerList = lap.getLapTimeAll();
        if (runTrackerList == null) {
            runTrackerList = new ArrayList<>();
        }
        runTrackerList.add(runTracker);
        lap.setLapTimeAll(runTrackerList);
        lapTracker.put(tag, lap);
        this.timerState = LapTimer.State.STOPPED;
        return currentTime;
    }

    /**
     * Get lap count.
     *
     * @param tag Timer tag name
     * @return Lap count
     */
    public int getLapCount(final String tag) {
        if (this.timerState == State.UNSTARTED) {
            throw new IllegalStateException("Timer not started.");
        }
        return lapTracker.get(tag).getLapCount();
    }

    /**
     * Get time difference.
     *
     * @param tag      Timer tag.
     * @param timeUnit Conversion time unit target.
     * @return Time difference.
     */
    public long getDiffTrackTime(final String tag, final TimeUnit timeUnit) {
        if (this.timerState == LapTimer.State.STOPPED) {
            var time = 0L;
            if (lapTracker.containsKey(tag)) {
                var lap = lapTracker.get(tag).getLapTime();
                if (lap.getStopTime() != 0) {
                    time = lap.getTimeDiff();
                }
            }
            return timeUnit.convert(time, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("Timer is not stopped.");
        }
    }

    /**
     * Get time difference.
     *
     * @param tag Timer tag.
     * @return Time difference.
     */
    public long getDiffTrackTime(final String tag) {
        return getDiffTrackTime(tag, TimeUnit.MILLISECONDS);
    }

    /**
     * Get total time.
     *
     * @param tag Timer tag.
     * @return Total time.
     */
    public long getTotalTime(final String tag) {
        return getTotalTime(tag, TimeUnit.MILLISECONDS);
    }

    /**
     * Get total time.
     *
     * @param tag      Timer tag.
     * @param timeUnit Conversion time unit target.
     * @return Total time.
     */
    public long getTotalTime(final String tag, final TimeUnit timeUnit) {
        var totalTime = 0L;
        if (this.timerState == LapTimer.State.STOPPED) {
            if (lapTracker != null && lapTracker.containsKey(tag)) {
                totalTime = lapTracker.get(tag).getTotalTime();
            }
        } else {
            throw new IllegalStateException("Timer is not stopped.");
        }
        return timeUnit.convert(totalTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Get average time per lap.
     *
     * @param tag Timer tag.
     * @return Average time per lap.
     */
    public long getAverageTimePerLap(final String tag) {
        return getAverageTimePerLap(tag, TimeUnit.MILLISECONDS);
    }

    /**
     * Get average time per lap.
     *
     * @param tag      Timer tag.
     * @param timeUnit Conversion time unit target.
     * @return Average time per lap.
     */
    public long getAverageTimePerLap(final String tag,
                                     final TimeUnit timeUnit) {
        var average = 0L;
        if (this.timerState == LapTimer.State.STOPPED) {
            var lap = lapTracker.get(tag);
            var lapList = lap.getLapTimeAll();
            if (lapList != null && !lapList.isEmpty()) {
                average = (long) (float)
                        lapTracker.get(tag).getTotalTime() / lapList.size();
            }
        } else {
            throw new IllegalStateException("Timer is not stopped.");
        }
        return timeUnit.convert(average, TimeUnit.MILLISECONDS);
    }

    /**
     * Get average time.
     *
     * @param tag Timer tag.
     * @return Average time.
     */
    public long getAverageTime(final String tag) {
        return getAverageTime(tag, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets average time.
     *
     * @param tag      Timer tag.
     * @param timeUnit Conversion time unit target.
     * @return Average time.
     */
    public long getAverageTime(final String tag, final TimeUnit timeUnit) {
        var time = 0L;
        if (this.timerState == LapTimer.State.STOPPED) {
            time = getAverageTimePerLap(tag, timeUnit) * getLapCount(tag);
        } else {
            throw new IllegalStateException("Timer is not stopped.");
        }
        return timeUnit.convert(time, TimeUnit.MILLISECONDS);
    }
}
