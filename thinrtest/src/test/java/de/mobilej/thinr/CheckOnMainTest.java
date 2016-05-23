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
public class CheckOnMainTest {

    @Test(expected = IllegalAccessError.class)
    public void testNewTaskOnWrongThread() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(mock(Thread.class));
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.task(mock(Context.class), "name", Activity.class, String.class)
                .inBackground(String.class, null);
    }

    @Test(expected = IllegalAccessError.class)
    public void testCancelOnWrongThread() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(mock(Thread.class));
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.cancel("foo", "bar");
    }

    @Test(expected = IllegalAccessError.class)
    public void testResumeOnWrongThread() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(mock(Thread.class));
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.onResume("foo", null);
    }

    @Test(expected = IllegalAccessError.class)
    public void testPauseOnWrongThread() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(mock(Thread.class));
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        Thinr.onPause("foo");
    }

}
