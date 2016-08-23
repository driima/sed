/*
 * Copyright (c) 2016 Dreamburst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreamburst.sed;

/**
 * Dispatches events. Events that are dispatched can be handled by explicitly defined
 * listeners or event handlers.
 */
public interface Dispatcher {

    /**
     * Dispatch an event with this dispatcher.
     *
     * @param event     Event to dispatch
     * @return          the event after all associated event handlers have been executed
     */
    <T extends Event> T dispatch(T event);

    /** Resets this dispatcher */
    void unregisterAll();
}
