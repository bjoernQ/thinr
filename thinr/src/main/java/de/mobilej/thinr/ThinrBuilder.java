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

/**
 * Builder Interface
 * <p>
 * Created by bjoern on 15.05.2016.
 */
public interface ThinrBuilder<T, P, I> {

    /**
     * Adds a function to be executed in the background.
     *
     * @param returnType explicit return type
     * @param function   the function
     * @return the job for further configuration
     */
    @MainThread
    <R> ThinrBuilder<T, R, I> inBackground(Class<R> returnType, ThinrFunctionInBackground<R, ? super P> function);

    /**
     * Adds a function to be executed in the background.
     *
     * @param function the function
     * @return the job for further configuration
     */
    @MainThread
    <R> ThinrBuilder<T, R, I> inBackground(ThinrFunctionInBackground<R, ? super P> function);

    /**
     * Adds a function to end this task in the background.
     *
     * @param function the function
     * @return the job for further configuration
     */
    @MainThread
    <R> ThinrFinalBuilder<T, R, I> endsInBackground(ThinrFinalFunctionInBackground<? super P> function);

    /**
     * Adds a function to be executed on main.
     *
     * @param returnType explicit return type
     * @param function   the function
     * @return the job for further configuration
     */
    @MainThread
    <R> ThinrBuilder<T, R, I> onMain(Class<R> returnType, ThinrFunctionOnMain<R, ? super T, ? super P> function);

    /**
     * Adds a function to be executed on main.
     *
     * @param function the function
     * @return the job for further configuration
     */
    @MainThread
    <R> ThinrBuilder<T, R, I> onMain(ThinrFunctionOnMain<R, ? super T, ? super P> function);

    /**
     * Adds a function to be executed on main as the last element.
     *
     * @param function the function
     * @return the job for further configuration
     */
    @MainThread
    <R> ThinrFinalBuilder<T, R, I> endsOnMain(ThinrFinalFunctionOnMain<? super T, ? super P> function);

}
