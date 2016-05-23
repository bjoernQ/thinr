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
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests the cancelling Thinr functionality.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Thinr.class, Looper.class, SystemClock.class, android.os.Process.class, ABridge.class})
public class CancelFlow3Test extends TestBase {

    private static String staticResult;

    @Test
    public void testCancel() throws Exception {
        Thinr.task(mock(Context.class), "name", CancelFlow3Test.class, String.class)
                .endsInBackground((appCtx, param, flowControl) -> {
                    CancelFlow3Test.staticResult = "cancel" + flowControl.isCancelled();
                })
                .execute("startParam", "componentB");

        Thinr.onResume("componentB", this);

        assertEquals(1, asyncTasks.size());

        Thinr.cancel("name", "componentB");
        doNextAsyncTaskRunBackgroundEvenIfCancelled(asyncTasks);

        assertEquals(0, asyncTasks.size());
        assertEquals(0, runnables.size());

        assertEquals("canceltrue", staticResult);

    }


}
