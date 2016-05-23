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

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.junit.After;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import de.mobilej.ABridge;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Base Class for tests serving some utilities
 * <p>
 * Created by bjoern on 17.05.2016.
 */
public class TestBase {

    protected String field = "field";
    protected String result;

    protected Queue<AsyncTaskHolder> asyncTasks;
    protected Queue<Runnable> runnables;
    //static Queue<Runnable> runnables;
    public static Handler mockHandler = mock(Handler.class);

    @Before
    public void setup() throws Exception {
        Field sdkIntField = Build.VERSION.class.getDeclaredField("SDK_INT");
        sdkIntField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() & ~Modifier.FINAL);

        sdkIntField.set(null, getSdkVersionForBuildVersion());

        mockStatic(ABridge.class);

        asyncTasks = new ArrayBlockingQueue<>(10);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AsyncTaskHolder holder = new AsyncTaskHolder();
                holder.asyncTask = (AsyncTask) invocation.getArguments()[1];
                holder.params = (Object[]) invocation.getArguments()[2];
                asyncTasks.add(holder);
                return null;
            }
        }).when(ABridge.class, "callObject", eq("android.os.AsyncTask.executeOnExecutor(java.util.concurrent.Executor,java.lang.Object[])"), anyObject(), any(Object[].class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AsyncTaskHolder holder = new AsyncTaskHolder();
                holder.asyncTask = (AsyncTask) invocation.getArguments()[1];
                holder.params = (Object[]) invocation.getArguments()[2];
                asyncTasks.add(holder);
                return null;
            }
        }).when(ABridge.class, "callObject", eq("android.os.AsyncTask.execute(java.lang.Object[])"), anyObject(), any(Object[].class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                for (AsyncTaskHolder holder : asyncTasks) {
                    if (holder.asyncTask == invocation.getArguments()[1]) {
                        holder.canceled = true;
                    }
                }

                return null;
            }
        }).when(ABridge.class, "callBoolean", eq("android.os.AsyncTask.cancel(boolean)"), anyObject(), any(Object[].class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                for (AsyncTaskHolder holder : asyncTasks) {
                    if (holder.asyncTask == invocation.getArguments()[1]) {
                        return holder.canceled;
                    }
                }

                return false;
            }
        }).when(ABridge.class, "callBoolean", eq("android.os.AsyncTask.isCancelled()"), anyObject(), any(Object[].class));


        Looper mockMainLooper = mock(Looper.class);
        when(mockMainLooper.getThread()).thenReturn(Thread.currentThread());
        mockStatic(Looper.class);
        when(Looper.getMainLooper()).thenReturn(mockMainLooper);

        mockNewHandler();
    }

    protected int getSdkVersionForBuildVersion() {
        return Build.VERSION_CODES.HONEYCOMB;
    }

    @After
    public void tearDown() throws Exception {
        Field sdkIntField = Build.VERSION.class.getDeclaredField("SDK_INT");
        sdkIntField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() & ~Modifier.FINAL);

        sdkIntField.set(null, 0);
    }

    protected boolean doNextRunnable(Queue<Runnable> runnables) {
        if (runnables.size() > 0) {
            Runnable r = runnables.poll();
            r.run();
            return true;
        }

        return false;
    }

    protected void doNextAsyncTaskUncancelled(Queue<AsyncTaskHolder> asyncTasks) throws Exception {
        if (asyncTasks.size() > 0) {
            AsyncTaskHolder holder = asyncTasks.poll();
            AsyncTask asyncTask = holder.asyncTask;
            Object res = Whitebox.invokeMethod(asyncTask, "doInBackground", holder.params);
            Whitebox.invokeMethod(asyncTask, "onPostExecute", res);
        }
    }


    protected boolean doNextAsyncTask(Queue<AsyncTaskHolder> asyncTasks) throws Exception {
        if (asyncTasks.size() > 0) {
            AsyncTaskHolder holder = asyncTasks.poll();
            AsyncTask asyncTask = holder.asyncTask;

            Object res = null;
            if (!holder.canceled) {
                res = Whitebox.invokeMethod(asyncTask, "doInBackground", holder.params);
            }

            if (holder.canceled) {
                Whitebox.invokeMethod(asyncTask, "onCancelled");
                return false;
            } else {
                Whitebox.invokeMethod(asyncTask, "onPostExecute", res);
            }

            return true;
        }

        return false;
    }


    protected boolean doNextAsyncTaskRunBackgroundEvenIfCancelled(Queue<AsyncTaskHolder> asyncTasks) throws Exception {
        if (asyncTasks.size() > 0) {
            AsyncTaskHolder holder = asyncTasks.peek();
            AsyncTask asyncTask = holder.asyncTask;

            Object res = Whitebox.invokeMethod(asyncTask, "doInBackground", holder.params);
            asyncTasks.poll();

            if (holder.canceled) {
                Whitebox.invokeMethod(asyncTask, "onCancelled");
                return false;
            } else {
                Whitebox.invokeMethod(asyncTask, "onPostExecute", res);
            }

            return true;
        }

        return false;
    }

    private Queue<Runnable> mockNewHandler() throws Exception {
        runnables = new ArrayBlockingQueue<>(10);

        whenNew(Handler.class).withAnyArguments().thenReturn(mockHandler);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                runnables.add((Runnable) invocation.getArguments()[0]);
                return null;
            }
        }).when(mockHandler).post(any(Runnable.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                runnables.remove((Runnable) invocation.getArguments()[0]);
                return null;
            }
        }).when(mockHandler).removeCallbacks(any(Runnable.class));

        return runnables;
    }

    protected void runQueuesEmpty() throws Exception {
        while (doNextRunnable(runnables) || doNextAsyncTask(asyncTasks)) {
            // run till completion
        }
    }

    public static class AsyncTaskHolder {
        public AsyncTask asyncTask;
        public Object[] params;
        public boolean canceled;
    }
}
