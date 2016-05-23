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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import de.mobilej.thinrtest.R;

import static junit.framework.Assert.assertEquals;

/**
 * We don't want leaks and crashes.
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(AndroidJUnit4.class)
public class CancelWithOnCancelTest {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void testNoLeaksAndCrashes() throws Exception {
        final int iterations = 10;


        Intent testActivityIntent = new Intent();
        testActivityIntent.setClassName("de.mobilej.thinr", "de.mobilej.thinr.TestActivity");
        testActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Activity activityUnderTest = instrumentation.startActivitySync(testActivityIntent);


        final Activity[] activityHolder = new Activity[1];
        for (int i = 1; i <= iterations; i++) {
            final View button = activityUnderTest.findViewById(R.id.button);
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    button.performClick();
                }
            });

            Activity recreatedActivity = activityUnderTest;
            activityUnderTest = null;
            boolean canceled = false;
            long t1 = SystemClock.uptimeMillis();
            for (int j = 0; j < i; j++) {
                Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor("de.mobilej.thinr.TestActivity", null, true);
                instrumentation.addMonitor(monitor);

                activityHolder[0] = recreatedActivity;

                SystemClock.sleep((long) (4500 / i)); // delay the recreation

                if (!canceled && (SystemClock.uptimeMillis() - t1) > 500) {
                    final View button2 = recreatedActivity.findViewById(R.id.button2);
                    instrumentation.runOnMainSync(new Runnable() {
                        @Override
                        public void run() {
                            button2.performClick();
                        }
                    });
                    canceled = true;
                }

                instrumentation.runOnMainSync(new Runnable() {
                    @Override
                    public void run() {
                        activityHolder[0].recreate();
                    }
                });
                instrumentation.waitForIdleSync();
                recreatedActivity = monitor.waitForActivity();
                instrumentation.removeMonitor(monitor);
            }

            SystemClock.sleep(2500);
            assertEquals(false, recreatedActivity.isDestroyed());

            final TextView text1 = (TextView) recreatedActivity.findViewById(R.id.text);
            final TextView text2 = (TextView) recreatedActivity.findViewById(R.id.text2);

            assertEquals("cancel", text1.getText());
            assertEquals("cancel", text2.getText());


            // check Activity doesn't leak
            activityUnderTest = null;
            activityHolder[0] = null;
            System.gc();
            System.runFinalization();
            System.gc();

            Class<?> vmDebugClass = Class.forName("dalvik.system.VMDebug");
            Method countInstancesOfClass = vmDebugClass.getDeclaredMethod("countInstancesOfClass", Class.class, Boolean.TYPE);
            long instances = (long) countInstancesOfClass.invoke(null, Class.forName("de.mobilej.thinr.TestActivity"), false);
            assertEquals(1, instances);

            activityUnderTest = recreatedActivity;
        }

        // cleanup
        activityHolder[0] = activityUnderTest;
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityHolder[0].finish();
            }
        });
        instrumentation.waitForIdleSync();
    }

}
