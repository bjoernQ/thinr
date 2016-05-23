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

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests Thinr
 * <p>
 * Created by bjoern on 12.05.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Looper.class})
public class CheckValidFunctionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testLambdaNull() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.task(mock(Context.class), "name", Activity.class, String.class)
                .inBackground(String.class, null);
    }

    @Test
    public void testLambda() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.task(mock(Context.class), "name", Activity.class, String.class)
                .inBackground(String.class, (appCtx, param, flowCtrl) -> "Result");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testIllegalLambda() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        int a = 5;
        Thinr.task(mock(Context.class), "name", Activity.class, String.class)
                .inBackground(String.class, (appCtx, param, flowCtrl) -> "Result" + a);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testInnerclass() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        ThinrFunctionInBackground<String, ? super String> innerClass = new ThinrFunctionInBackground<String, String>() {
            @Override
            public String function(Context appCtx, String param, FlowControl flowControl) {
                return null;
            }
        };
        Thinr.task(mock(Context.class), "name", Activity.class, String.class)
                .inBackground(String.class, innerClass);
    }

    @Test
    public void testNestedclass() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.task(mock(Context.class), "name", Activity.class, String.class)
                .inBackground(String.class, new NestedClass());
    }

    public static class NestedClass implements ThinrFunctionInBackground<String, String> {
        @Override
        public String function(Context appCtx, String param, FlowControl flowControl) {
            return null;
        }
    }
}
