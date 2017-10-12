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

import android.support.annotation.MainThread;

import java.util.concurrent.Executor;

/**
 * Builder Interface
 * <p>
 * Created by bjoern on 15.05.2016.
 */
public interface ThinrFinalBuilder<T, P, I> {

    /**
     * Starts the execution of the job.
     *
     * @param param       the parameter passed to the first function
     * @param componentId Id to identify this job (e.g. to cancel it)
     * @return true if the job will execute, false if another job with the same id is already running
     */
    @MainThread
    boolean execute(I param, String componentId);

    /**
     * Starts the execution of the job.
     *
     * @param param       the parameter passed to the first function
     * @param componentId Id to identify this job (e.g. to cancel it)
     * @param executor the executor to use for background operations
     * @return true if the job will execute, false if another job with the same id is already running
     */
    @MainThread
    boolean execute(I param, String componentId,Executor executor);

    /**
     * Sets a function to be executed on the main thread if this job is canceled.
     *
     * @param function function to be executed
     */
    @MainThread
    <R> ThinrFinalBuilderWithoutOnCancel<T, R, I> onCancel(ThinrOnCancelFunctionOnMain<? super T, ? super P> function);

}
