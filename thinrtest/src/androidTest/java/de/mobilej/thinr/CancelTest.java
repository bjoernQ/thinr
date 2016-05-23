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

import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * A simple test for canceling a flow.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(AndroidJUnit4.class)
public class CancelTest {

    public String result;

    @Test
    public void testFlow() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.task(InstrumentationRegistry.getContext(), "task", CancelTest.class, String.class)
                        .onMain(String.class, (target, param1) -> {
                            target.result = param1 + "main1";
                            return param1 + "main1";
                        })
                        .inBackground(String.class, (appCtx, param, flowCtrl) -> {
                            SystemClock.sleep(1000);
                            return param + "back1";
                        })
                        .endsOnMain((target1, param2) -> {
                            target1.result = param2 + "main2";
                        })
                        .execute("start", "self");

                assertTrue(Thinr.isRunning("task", "self"));
                Thinr.onResume("self", CancelTest.this);
            }
        });

        SystemClock.sleep(100);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertTrue(Thinr.isRunning("task", "self"));

                Thinr.cancel("task", "self");
                assertFalse(Thinr.isRunning("task", "self"));

                assertEquals("startmain1", result);

                // check that it doesn't restart
                result = null;
                Thinr.onPause("self");
                Thinr.onResume("self", CancelTest.this);
            }
        });

        SystemClock.sleep(1500);
        assertEquals(null, result);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.onPause("self");
            }
        });
    }
}
