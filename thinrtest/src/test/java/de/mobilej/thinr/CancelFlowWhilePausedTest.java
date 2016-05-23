/*
 *    Copyright (C) 2016 Björn Quentin
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.mobilej.thinr;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.mobilej.ABridge;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests the cancelling Thinr functionality.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Thinr.class, Looper.class, SystemClock.class, android.os.Process.class, ABridge.class})
public class CancelFlowWhilePausedTest extends TestBase {

    @Test
    public void testCancelInBackground() throws Exception {

        Thinr.task(mock(Context.class), "name", CancelFlowWhilePausedTest.class, String.class)
                .inBackground(String.class, (appCtx, param, flowCtrl) -> "#1(" + param + ")")
                .onMain(String.class, (target, param1) -> {
                    target.result = "#2(" + param1 + target.field + ")";
                    return "#2(" + param1 + target.field + ")";
                })
                .inBackground(String.class, (appCtx2, param2, flowCtrl) -> "#3(" + param2 + ")")
                .endsOnMain((target1, param3) -> {
                    target1.result = "#4(" + param3 + target1.field + ")";
                })
                .execute("startParam", "componentB");

        assertTrue(Thinr.isRunning("name", "componentB"));
        Thinr.onResume("componentB", this);
        assertTrue(Thinr.isRunning("name", "componentB"));

        assertTrue(doNextAsyncTask(asyncTasks));
        assertTrue(doNextRunnable(runnables));
        assertTrue(Thinr.isRunning("name", "componentB"));
        Thinr.onPause("componentB");
        Thinr.cancel("name", "componentB");
        assertFalse(Thinr.isRunning("name", "componentB"));
        Thinr.onResume("componentB", this);
        assertFalse(Thinr.isRunning("name", "componentB"));
        assertFalse(doNextAsyncTask(asyncTasks));
        assertFalse(doNextRunnable(runnables));
        assertFalse(doNextAsyncTask(asyncTasks));

        assertEquals("#2(#1(startParam)field)", result);

        Thinr.onPause("componentB");
    }


    @Test
    public void testCancelMain() throws Exception {

        Thinr.task(mock(Context.class), "name", CancelFlowWhilePausedTest.class, String.class)
                .inBackground(String.class, (appCtx, param, flowCtrl) -> "#1(" + param + ")")
                .onMain(String.class, (target, param1) -> {
                    target.result = "#2(" + param1 + target.field + ")";
                    return "#2(" + param1 + target.field + ")";
                })
                .inBackground(String.class, (appCtx1, param2, flowCtrl) -> "#3(" + param2 + ")")
                .endsOnMain((target1, param3) -> {
                    target1.result = "#4(" + param3 + target1.field + ")";
                })
                .execute("startParam", "componentC");

        Thinr.onResume("componentC", this);

        assertTrue(doNextAsyncTask(asyncTasks));
        Thinr.onPause("componentC");
        Thinr.cancel("name", "componentC");
        Thinr.onResume("componentC", this);
        assertFalse(doNextRunnable(runnables));
        assertFalse(doNextAsyncTask(asyncTasks));
        assertFalse(doNextRunnable(runnables));
        assertFalse(doNextAsyncTask(asyncTasks));

        assertEquals(null, result);

        Thinr.onPause("componentC");
    }

}
