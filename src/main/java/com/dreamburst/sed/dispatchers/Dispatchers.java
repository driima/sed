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

package com.dreamburst.sed.dispatchers;

import com.dreamburst.sed.Dispatcher;
import com.dreamburst.sed.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores dispatchers in a map. Automatically creates a new Dispatcher of the specified type if one is not
 * already present. Events can be dispatched by individual dispatchers by providing the associated dispatcher type,
 * and can also be dispatched by all dispatchers.
 */
public final class Dispatchers {

    private static final Map<Class<? extends Dispatcher>, Dispatcher> dispatchers = new HashMap<>();

    public static void add(Dispatcher dispatcher) {
        dispatchers.put(dispatcher.getClass(), dispatcher);
    }

    /**
     * Adds a new instance of a dispatcher to the dispatcher map.
     *
     * @param type      Type of dispatcher to add
     */
    public static void add(Class<? extends Dispatcher> type) {
        try {
            dispatchers.put(type, type.getConstructor().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void remove(Dispatcher dispatcher) {
        remove(dispatcher.getClass());
    }

    public static void remove(Class<? extends Dispatcher> type) {
        dispatchers.remove(type);
    }

    /**
     * Returns the dispatcher of the specified type. If the dispatcher doesn't exist, it is created and added to
     * the dispatcher map.
     *
     * @param type      Type of dispatcher to return
     * @return          Dispatcher of the specified type
     */
    public static <T extends Dispatcher> T get(Class<T> type) {
        if (!has(type)) {
            add(type);
        }

        return (T) dispatchers.get(type);
    }

    public static boolean has(Class<? extends Dispatcher> type) {
        return dispatchers.containsKey(type);
    }

    /**
     * Dispatches an event to all dispatchers.
     *
     * @param event     Event to dispatch
     * @return          Event, after dispatched by all dispatchers
     */
    public static <T extends Event> T dispatch(T event) {
        dispatchers.values().forEach(dispatcher -> dispatcher.dispatch(event));

        return event;
    }

    /**
     * Dispatches an event by a specific dispatcher.
     *
     * @param event     Event to dispatch
     * @param type      Type of dispatcher the event is to be dispatched by
     * @return          Event, after dispatched by the dispatcher
     */
    public static <T extends Event> T dispatch(T event, Class<? extends Dispatcher> type) {
        get(type).dispatch(event);

        return event;
    }
}
