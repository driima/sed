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
import com.dreamburst.sed.EventHandler;
import com.dreamburst.sed.Handler;

import java.util.*;

/**
 * Registers events and dispatches Events to any Listeners that have EventHandlers associated to those Events.
 * Sorts event handlers based on priority.
 */
public class DirectDispatcher implements Dispatcher {

    private static final HandlerPriorityComparator priority = new HandlerPriorityComparator();

    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> registeredHandlers = new HashMap<>();

    public Map<Class<? extends Event>, List<EventHandler<? extends Event>>> getRegisteredHandlers() {
        return registeredHandlers;
    }

    public <T extends Event> List<EventHandler<? extends Event>> getHandlers(Class<T> type) {
        if (!registeredHandlers.containsKey(type)) {
            registeredHandlers.put(type, new ArrayList<>());
        }

        return registeredHandlers.get(type);
    }

    public <T extends Event> void register(Class<T> type, EventHandler<T> handler) {
        List<EventHandler<? extends Event>> handlers = getHandlers(type);

        handlers.add(handler);
        handlers.sort(priority);
    }

    /**
     * Unregisters the EventHandler from the specified event type registered with this dispatcher.
     *
     * @param type    Event type to unregister the EventHandler from
     * @param handler EventHandler to unregister
     * @return {@code true} if the EventHandler was previously registered
     */
    public <T extends Event> boolean unregister(Class<T> type, EventHandler<T> handler) {
        return registeredHandlers.containsKey(type) && getHandlers(type).remove(handler);
    }

    /**
     * Unregisters every EventHandler registered for the specified event type.
     *
     * @param type Event type to unregister associated EventHandlers from
     * @return List of EventHandlers previously registered to the specified event type
     */
    public List<EventHandler<? extends Event>> unregister(Class<? extends Event> type) {
        if (registeredHandlers.containsKey(type)) {
            return registeredHandlers.remove(type);
        }

        return null;
    }

    /**
     * Unregisters every registered {@link Event}.
     */
    @Override
    public void unregisterAll() {
        registeredHandlers.clear();
    }

    /**
     * Dispatch an event to each {@link EventHandler} for the event type registered with this dispatcher.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> T dispatch(T event) {
        getHandlers(event.getClass()).forEach(handler -> ((EventHandler<T>) handler).execute(event));

        return event;
    }

    private static int getHandlerPriority(EventHandler handler) {
        int priority = 0;

        try {
            if (handler.getClass().getAnnotation(Handler.class) != null) {
                priority = handler.getClass().getAnnotation(Handler.class).priority();
            } else {
                priority = handler.getClass().getMethod("execute", Event.class).getAnnotation(Handler.class).priority();
            }
        } catch (Exception ignored) {
        }

        return priority;
    }

    private static class HandlerPriorityComparator implements Comparator<EventHandler> {
        @Override
        public int compare(EventHandler one, EventHandler two) {
            return getHandlerPriority(one) - getHandlerPriority(two);
        }
    }
}
