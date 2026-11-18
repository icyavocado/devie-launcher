package com.evie.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(application = android.app.Application.class, sdk = 33)
public class SearchDebouncerTest {

    @Test
    public void onlyTheLatestScheduledRunnableRuns() {
        SearchDebouncer d = new SearchDebouncer(150);
        int[] runs = new int[2];
        d.schedule(() -> runs[0]++);
        d.schedule(() -> runs[1]++);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(0, runs[0]);
        assertEquals(1, runs[1]);
    }

    @Test
    public void cancelPreventsRun() {
        SearchDebouncer d = new SearchDebouncer(150);
        int[] runs = new int[1];
        d.schedule(() -> runs[0]++);
        d.cancel();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals(0, runs[0]);
    }

    @Test
    public void immediateQueriesRun() {
        SearchDebouncer d = new SearchDebouncer(150);
        int[] runs = new int[1];
        d.runNow(() -> runs[0]++);
        assertEquals(1, runs[0]);
    }
}
