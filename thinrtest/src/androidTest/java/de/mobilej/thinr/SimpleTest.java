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

/**
 * A simple test.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(AndroidJUnit4.class)
public class SimpleTest {

    public String result;

    @Test
    public void testFlow() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.task(InstrumentationRegistry.getContext(), "task", SimpleTest.class, String.class)
                        .onMain(String.class, (target, param1) -> {
                            return param1 + "main1";
                        })
                        .inBackground(String.class, (appCtx, param, flowCtrl) -> param + "back1")
                        .endsOnMain((target1, param2) -> {
                            target1.result = param2 + "main2";
                        })
                        .execute("start", "self");

                Thinr.onResume("self", SimpleTest.this);
            }
        });

        SystemClock.sleep(500);
        assertEquals("startmain1back1main2", result);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.onPause("self");
            }
        });
    }

}