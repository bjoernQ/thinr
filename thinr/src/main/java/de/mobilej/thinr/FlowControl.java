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

/**
 * Passed to background functions.
 * <p>
 * Created by bjoern on 17.05.2016.
 */
public interface FlowControl {

    /**
     * Query if this job has been canceled.
     *
     * @return true if the job is already canceled
     */
    boolean isCancelled();

}
