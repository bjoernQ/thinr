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

import android.os.ConditionVariable;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the cancelling Thinr functionality.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(AndroidJUnit4.class)
public class CancelFlowAndroidTest {

    private String field = "field";
    private String result;
    public static String sResult;
    public static ConditionVariable barrier = new ConditionVariable(false);
    private static Thread MAIN_THREAD;

    @Test
    public void testLambda() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.task(InstrumentationRegistry.getContext(), "name", CancelFlowAndroidTest.class, String.class)
                        .inBackground(String.class, (appCtx, param, flowCtrl) -> {
                            SystemClock.sleep(1000);
                            return "#1(" + param + ")";
                        })
                        .onMain(String.class, (target, param1) -> {
                            target.result = "#2(" + param1 + target.field + ")";
                            return "#2(" + param1 + target.field + ")";
                        })
                        .inBackground(String.class, (appCtx2, param2, flowCtrl) -> "#3(" + param2 + ")")
                        .endsOnMain((target1, param3) -> {
                            target1.result = "#2(" + param3 + target1.field + ")";
                        })
                        .execute("startParam", "component");

                Thinr.onResume("component", CancelFlowAndroidTest.this);
            }
        });


        SystemClock.sleep(100);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.cancel("name", "component");
            }
        });

        SystemClock.sleep(2000);
        assertEquals(null, result);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.onPause("component");
            }
        });

        SystemClock.sleep(2000);

    }


    @Test
    public void testLambda2() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                CancelFlowAndroidTest.sResult = null;
                CancelFlowAndroidTest.barrier.close();
                Thinr.task(InstrumentationRegistry.getContext(), "name2", CancelFlowAndroidTest.class, String.class)
                        .inBackground(String.class, (appCtx, param, flowCtrl) -> {
                            CancelFlowAndroidTest.sResult = "1";
                            return "#1(" + param + ")";
                        })
                        .onMain(String.class, (target, param1) -> {
                            CancelFlowAndroidTest.sResult = "2";
                            target.result = "#2(" + param1 + target.field + ")";
                            return "#2(" + param1 + target.field + ")";
                        })
                        .inBackground(String.class, (appCtx2, param2, flowCtrl) -> {
                            CancelFlowAndroidTest.barrier.open();
                            try {
                                Thread.sleep(1000);
                                CancelFlowAndroidTest.sResult = "3";
                            } catch (InterruptedException e) {
                                CancelFlowAndroidTest.sResult = "Interrupted";
                            }
                            return "#3(" + param2 + ")";
                        })
                        .endsOnMain((target1, param3) -> {
                            target1.result = "#2(" + param3 + target1.field + ")";
                        })
                        .execute("startParam", "component2");

                Thinr.onResume("component2", CancelFlowAndroidTest.this);
            }
        });


        assertTrue("Didn't reach barrier open.", CancelFlowAndroidTest.barrier.block(5000));

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.cancel("name2", "component2");
            }
        });

        SystemClock.sleep(2000);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.onPause("component2");
            }
        });

        assertEquals("#2(#1(startParam)field)", result);
        assertEquals("Interrupted", CancelFlowAndroidTest.sResult);
    }


    @Test
    public void testLambda3() throws Exception {

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                CancelFlowAndroidTest.sResult = null;
                CancelFlowAndroidTest.barrier.close();
                Thinr.task(InstrumentationRegistry.getContext(), "name3", CancelFlowAndroidTest.class, String.class)
                        .inBackground(String.class, (appCtx, param, flowCtrl) -> {
                            CancelFlowAndroidTest.sResult = "1";
                            return "#1(" + param + ")";
                        })
                        .onMain(String.class, (target, param1) -> {
                            CancelFlowAndroidTest.sResult = "2";
                            CancelFlowAndroidTest.barrier.open();
                            SystemClock.sleep(1000);
                            target.result = "#2(" + param1 + target.field + ")";
                            return "#2(" + param1 + target.field + ")";
                        })
                        .inBackground(String.class, (appCtx2, param2, flowCtrl) -> {
                            CancelFlowAndroidTest.sResult = "3";
                            return "#3(" + param2 + ")";
                        })
                        .endsOnMain((target1, param3) -> {
                            CancelFlowAndroidTest.sResult = "4";
                            target1.result = "#2(" + param3 + target1.field + ")";
                        })
                        .execute("startParam", "component3");

                CancelFlowAndroidTest.sResult = null;
                Thinr.onResume("component3", CancelFlowAndroidTest.this);
            }
        });


        SystemClock.sleep(100);

        assertTrue("Didn't reach barrier open.", CancelFlowAndroidTest.barrier.block(5000));

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.cancel("name3", "component3");
            }
        });

        SystemClock.sleep(2000);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Thinr.onPause("component3");
            }
        });

        SystemClock.sleep(2000);
        assertEquals("2", CancelFlowAndroidTest.sResult);
        assertEquals("#2(#1(startParam)field)", result);
    }
}
