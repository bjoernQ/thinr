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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests the basic Thinr functionality.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Thinr.class, Looper.class, SystemClock.class, android.os.Process.class, ABridge.class})
public class ExecuteWithTheSameIdInParallelTest extends TestBase {

    @Test
    public void testExecution() throws Exception {

        assertTrue(Thinr.task(mock(Context.class), "name", ExecuteWithTheSameIdInParallelTest.class, String.class)
                .endsOnMain((target, param) -> {
                    target.result = "done1";
                })
                .execute("startParam", "componentB"));

        assertFalse(Thinr.task(mock(Context.class), "name", ExecuteWithTheSameIdInParallelTest.class, String.class)
                .endsOnMain((target, param) -> {
                    target.result = "done2";
                })
                .execute("startParam", "componentB"));


    }

}
