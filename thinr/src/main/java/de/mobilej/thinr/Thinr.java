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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * THis Is Not Reactive
 * <p>
 * To take full advantage of this you should use Java 8: http://developer.android.com/preview/j8-jack.html
 * <p>
 * Created by bjoern on 10.05.2016.
 *
 * @param <T> target component type
 * @param <P> initial result type
 * @param <I> initial param type
 */
public final class Thinr<T, P, I> implements ThinrBuilder<T, P, I>, ThinrFinalBuilder<T, P, I>, ThinrFinalBuilderWithoutOnCancel<T, P, I> {

    private static final Looper MAIN_LOOPER = Looper.getMainLooper();
    private static final Handler HANDLER = new Handler(MAIN_LOOPER);
    private static final String TAG = "Thinr";

    private static HashMap<String, HashMap<String, Thinr>> componentIdToThinrInstances = new HashMap<>();
    private static Map<String, Object> activeComponentIdsToTargets = Collections.synchronizedMap(new HashMap<String, Object>());
    private static boolean runtimeChecksEnabled = true;

    private final Context appCtx;
    private Object currentValue;
    private String componentId;
    private String name;
    private boolean operationRunning;
    private boolean suspended;
    private boolean canceled;
    private Queue<ThinrFunction> functions = new LinkedBlockingQueue<>();
    private Runnable postedRunnable;
    private AsyncTask<Object, Void, Object> startedAsyncTask;
    private ThinrOnCancelFunctionOnMain cancelFunction;

    private Thinr(Context appCtx) {
        this.appCtx = appCtx;
    }

    /**
     * Create a new task.
     *
     * @param <X>                target type
     * @param <Z>                parameter and return type
     * @param context            a context, getApplicationContext will be called and that is passed to background operations
     * @param name               id of the task
     * @param targetType         the target type (e.g. the Activity, Fragment etc.)
     * @param paramAndReturnType the type used for the sub-task's parameter and return value    @return a job
     */
    @SuppressWarnings("unused")
    @MainThread
    public static <X, Z> ThinrBuilder<X, Z, Z> task(Context context, final String name, final Class<X> targetType, final Class<Z> paramAndReturnType) {
        checkMainThread();

        Thinr<X, Z, Z> thinr = new Thinr<>(context.getApplicationContext());
        thinr.name = name;
        return thinr;
    }

    /**
     * Adds a function to be executed in the background.
     *
     * @param function the function
     * @return the job for further configuration
     */
    @Override
    @SuppressWarnings("unchecked")
    @MainThread
    public <R> ThinrBuilder<T, R, I> inBackground(final Class<R> returnType, final ThinrFunctionInBackground<R, ? super P> function) {
        checkValidFunction(function);
        functions.add(function);
        return (Thinr<T, R, I>) this;
    }

    @Override
    @MainThread
    public <R> ThinrBuilder<T, R, I> inBackground(final ThinrFunctionInBackground<R, ? super P> function) {
        return inBackground(null, function);
    }

    @Override
    @SuppressWarnings("unchecked")
    @MainThread
    public <R> ThinrFinalBuilder<T, R, I> endsInBackground(final ThinrFinalFunctionInBackground<? super P> function) {
        checkValidFunction(function);
        functions.add(function);
        return (Thinr<T, R, I>) this;
    }

    /**
     * Adds a function to be executed on the main thread.
     *
     * @param function the function to be executed on the main thread
     * @return the job for further configuration
     */
    @Override
    @SuppressWarnings("unchecked")
    @MainThread
    public <R> ThinrBuilder<T, R, I> onMain(final Class<R> returnType, final ThinrFunctionOnMain<R, ? super T, ? super P> function) {
        checkValidFunction(function);
        functions.add(function);
        return (Thinr<T, R, I>) this;
    }

    @Override
    @MainThread
    public <R> ThinrBuilder<T, R, I> onMain(final ThinrFunctionOnMain<R, ? super T, ? super P> function) {
        return onMain(null, function);
    }

    @Override
    @SuppressWarnings("unchecked")
    @MainThread
    public <R> ThinrFinalBuilder<T, R, I> endsOnMain(final ThinrFinalFunctionOnMain<? super T, ? super P> function) {
        checkValidFunction(function);
        functions.add(function);
        return (Thinr<T, R, I>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @MainThread
    public <R> ThinrFinalBuilderWithoutOnCancel<T, R, I> onCancel(final ThinrOnCancelFunctionOnMain<? super T, ? super P> function) {
        checkValidFunction(function);
        cancelFunction = function;
        return (Thinr<T, R, I>) this;
    }

    /**
     * Throws an exception if the function will cause a leak.
     *
     * @param function what to check
     */
    private static void checkValidFunction(final Object function) {
        if (!Thinr.runtimeChecksEnabled) {
            return;
        }

        if (function == null) {
            throw new IllegalArgumentException("Don't pass NULL.");
        }

        Field[] functionFields = function.getClass().getDeclaredFields();
        if (functionFields.length > 0) {
            // workaround to not fail during instrumentation
            if (functionFields.length == 1 && "$jacocoData".equals(functionFields[0].getName())) {
                return;
            }

            // needed for Retrolambda
            if (functionFields.length == 1 && "instance".equals(functionFields[0].getName())) {
                return;
            }

            throw new IllegalArgumentException("Don't reference outer variables and fields from Lambdas / nested classes used here." + Arrays.toString(functionFields));
        }
    }

    /**
     * Hands over this job to be executed by the framework.
     *
     * @param param       the start parameter of the flow
     * @param componentId the id of the component to guard the execution
     * @return true if the flow is submitted to be processed, false otherwise (i.e. there is currently a flow with the same id and component)
     */
    @Override
    @MainThread
    public boolean execute(final I param, final String componentId) {
        checkMainThread();

        this.componentId = componentId;
        this.currentValue = param;
        HashMap<String, Thinr> instancesForComponent = componentIdToThinrInstances.get(componentId);
        if (instancesForComponent == null) {
            instancesForComponent = new HashMap<>();
            componentIdToThinrInstances.put(componentId, instancesForComponent);
        }

        if (instancesForComponent.get(this.name) != null) {
            return false;
        }

        instancesForComponent.put(this.name, this);
        next();

        return true;
    }

    @MainThread
    private void next() {
        checkMainThread();

        if (functions.size() > 0) {
            if (operationRunning) {
                return;
            }

            if (canceled) {
                if (cancelFunction != null) {
                    doNextOperationOnMain(cancelFunction);
                }

                return;
            }

            final ThinrFunction nextOperation = functions.peek();

            if (nextOperation instanceof ThinrInBackground) {
                doNextOperationInBackground(nextOperation);
            } else {
                doNextOperationOnMain(nextOperation);
            }
        } else {
            HashMap<String, Thinr> instances = componentIdToThinrInstances.get(componentId);
            if (instances != null) {
                instances.remove(this.name);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doNextOperationInBackground(final ThinrFunction nextOperation) {
        operationRunning = true;
        startedAsyncTask = new AsyncTask<Object, Void, Object>() {

            FlowControl flowControl = new FlowControl() {
                @Override
                public boolean isCancelled() {
                    return isAsyncTaskCancelled();
                }
            };

            boolean isAsyncTaskCancelled() {
                return isCancelled();
            }

            @Override
            protected final Object doInBackground(final Object... params) {
                functions.poll();
                Object res = null;
                try {
                    if (nextOperation instanceof ThinrFinalFunctionInBackground) {
                        res = null;
                        ((ThinrFinalFunctionInBackground) nextOperation).function(appCtx, currentValue, flowControl);
                    } else {
                        res = ((ThinrFunctionInBackground<Object, P>) nextOperation).function(appCtx, (P) currentValue, flowControl);
                    }
                } catch (Throwable e) {
                    Log.w(TAG, "Exception from background operation", e);
                }

                return res;
            }

            @Override
            protected void onPostExecute(final Object p) {
                currentValue = p;
                operationRunning = false;
                startedAsyncTask = null;
                next();
            }

            @Override
            protected void onCancelled() {
                operationRunning = false;
                startedAsyncTask = null;
                next();
            }

        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            startedAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentValue);
        } else {
            startedAsyncTask.execute(currentValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void doNextOperationOnMain(final ThinrFunction nextOperation) {
        T target = (T) activeComponentIdsToTargets.get(componentId);

        if (target == null) {
            suspended = true;
            return;
        }

        postedRunnable = new Runnable() {
            @Override
            public void run() {
                final T target = (T) activeComponentIdsToTargets.get(componentId);

                if (target == null) {
                    suspended = true;
                    return;
                }

                operationRunning = true;
                functions.poll();

                try {
                    if (nextOperation instanceof ThinrFinalFunctionOnMain) {
                        ((ThinrFinalFunctionOnMain<T, Object>) nextOperation).function(target, currentValue);
                        currentValue = null;
                    } else {
                        currentValue = ((ThinrFunctionOnMain<Object, T, Object>) nextOperation).function(target, currentValue);
                    }
                } catch (Throwable e) {
                    Log.w(TAG, "Exception from operation on main thread", e);
                    currentValue = null;
                }

                postedRunnable = null;
                operationRunning = false;

                if (!canceled) {
                    next();
                } else {
                    if (cancelFunction == nextOperation) {
                        componentIdToThinrInstances.get(componentId).remove(name);
                    }
                }
            }
        };
        HANDLER.post(postedRunnable);
    }

    @MainThread
    private void cancel() {
        canceled = true;

        if (postedRunnable != null) {
            HANDLER.removeCallbacks(postedRunnable);
            postedRunnable = null;
            next();
        }

        if (startedAsyncTask != null) {
            if (!startedAsyncTask.isCancelled()) {
                startedAsyncTask.cancel(true);
                startedAsyncTask = null;
            }
        }
    }

    /**
     * Call this to signal a "pause" of the given component.
     * <p>
     * No further execution will happen for this component.
     *
     * @param componentId id of the component
     */
    @MainThread
    public static void onPause(final String componentId) {
        checkMainThread();

        activeComponentIdsToTargets.remove(componentId);
    }

    /**
     * Call this to signal a "resume" of the given component.
     * <p>
     * This will start / continue flows for the given component.
     * This will also attach the given target to be used for the execution of sub-tasks.
     *
     * @param componentId id of the component
     * @param target      the target to be used
     */
    @MainThread
    public static void onResume(final String componentId, final Object target) {
        checkMainThread();

        activeComponentIdsToTargets.put(componentId, target);

        HashMap<String, Thinr> instances = componentIdToThinrInstances.get(componentId);
        if (instances != null) {
            for (Map.Entry<String, Thinr> entry : instances.entrySet()) {
                if (entry.getValue().suspended) {
                    entry.getValue().suspended = false;
                    entry.getValue().next();
                }
            }
        }
    }

    /**
     * Cancel the given job.
     * <p>
     * Will try to cancel a running background operation on no further sub-tasks will be executed.
     *
     * @param jobId       id of the job
     * @param componentId id of the component for the job
     */
    @MainThread
    public static void cancel(final String jobId, final String componentId) {
        checkMainThread();

        HashMap<String, Thinr> instances = componentIdToThinrInstances.get(componentId);
        if (instances != null) {
            Thinr instance = instances.get(jobId);
            if (instance != null) {
                instance.cancel();
                if (instance.cancelFunction == null) {
                    instances.remove(jobId);
                }
            }
        }
    }

    /**
     * Checks if the given job for the given component is currently "running".
     * It is running no matter if it's currently suspended or not.
     *
     * @param jobId       id of the job
     * @param componentId id of the component
     * @return true if there is a job, false otherwise
     */
    @MainThread
    public static boolean isRunning(final String jobId, final String componentId) {
        checkMainThread();

        HashMap<String, Thinr> instances = componentIdToThinrInstances.get(componentId);
        if (instances != null) {
            Thinr instance = instances.get(jobId);
            if (instance != null && !instance.canceled) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enable / Disable the (somewhat expensive) checks at runtime.
     * You might want to disable these checks for release builds while enabling them for debug builds.
     *
     * @param runtimeChecksEnabled true or false
     */
    public static void setRuntimeChecksEnabled(boolean runtimeChecksEnabled) {
        Thinr.runtimeChecksEnabled = runtimeChecksEnabled;
    }

    private static void checkMainThread() {
        if (MAIN_LOOPER.getThread() == Thread.currentThread()) {
            return;
        }

        throw new IllegalAccessError("Must be called on the Main thread.");
    }
}
