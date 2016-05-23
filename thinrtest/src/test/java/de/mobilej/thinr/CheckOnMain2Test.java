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
public class CheckOnMain2Test {

    @Test(expected = IllegalAccessError.class)
    public void testExecuteOnWrongThread() throws Exception {
        mockStatic(Looper.class);
        Looper mockLooper = mock(Looper.class);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        ThinrFinalBuilder<Activity, Object, String> task = Thinr.task(mock(Context.class), "CheckOnMain2Test", Activity.class, String.class)
                .endsOnMain((target, param) -> {

                });


        when(mockLooper.getThread()).thenReturn(mock(Thread.class));
        task.execute("start", "CheckOnMain2Test");

    }
}
