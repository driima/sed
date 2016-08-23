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
import com.dreamburst.sed.Handler;
import com.dreamburst.sed.Listener;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Registers Listeners and dispatches Events to any Listeners that have EventHandlers associated to those Events.
 * Sorts event handlers based on priority.
 */
public class BatchDispatcher implements Dispatcher {

    private static final ListenerPriorityComparator priority = new ListenerPriorityComparator();

    private Map<Listener, List<Method>> registeredListeners = new HashMap<>();

    public Map<Listener, List<Method>> getRegisteredListeners() {
        return registeredListeners;
    }

    /**
     * Registers a Listener and all of its event handler methods with this dispatcher.
     *
     * @param listener Listener to register
     */
    public void register(Listener listener) {
        registeredListeners.put(listener, getEventHandlers(listener));
    }

    /**
     * Unregisters a Listener from this dispatcher.
     *
     * @param listener Listener to unregister
     */
    public void unregister(Listener listener) {
        if (registeredListeners.containsKey(listener)) {
            registeredListeners.remove(listener);
            register(listener);
        }
    }

    /**
     * Unregisters every registered {@link Listener}.
     */
    @Override
    public void unregisterAll() {
        registeredListeners.clear();
    }

    /**
     * Dispatch an event to each {@link Listener} registered with this dispatcher.
     */
    @Override
    public <T extends Event> T dispatch(T event) {
        registeredListeners.keySet().forEach(listener -> dispatch(event, listener));

        return event;
    }

    private <T extends Event> void dispatch(T event, Listener listener) {
        List<Method> methods = registeredListeners.get(listener);

        for (Method method : methods) {
            if (method.getParameterCount() != 1) {
                continue;
            }

            try {
                if (event.getClass().isAssignableFrom(method.getParameterTypes()[0])) {
                    method.invoke(listener, event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<Method> getEventHandlers(Listener listener) {
        Method[] methods = listener.getClass().getDeclaredMethods();

        // Assure that the order of insertion is maintained by using LinkedList.
        List<Method> result = new LinkedList<>();

        for (Method method : methods) {
            if (isHandler(method)) {
                result.add(method);
            }
        }

        Collections.sort(result, priority);

        return result;
    }

    private boolean isHandler(Method method) {
        return method.getAnnotation(Handler.class) != null;
    }

    private static class ListenerPriorityComparator implements Comparator<Method> {
        @Override
        public int compare(Method one, Method two) {
            return one.getAnnotation(Handler.class).priority() - two.getAnnotation(Handler.class).priority();
        }
    }
}