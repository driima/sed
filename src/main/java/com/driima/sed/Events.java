package com.driima.sed;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public final class Events {

    private static long handlerCounter = 0;

    private static final HandlerPriorityComparator handlerPriority = new HandlerPriorityComparator();

    private final Map<Class<?>, List<EventHandler<?>>> registeredHandlers = new HashMap<>();
    private final Map<Long, EventHandler<?>> indexedHandlers = new HashMap<>();
    private final Map<ProviderDefinition, Function<Object, Object>> providedObjects = new HashMap<>();

    /**
     * Calls a new event.
     *
     * @param event  the event to call
     * @return       the event, after all handlers have acted on it
     */
    public <T> T call(T event) {
        List<EventHandler<?>> eventHandlers = registeredHandlers.get(event.getClass());

        if (eventHandlers != null) {
            for (EventHandler<?> eventHandler : eventHandlers) {
                ((EventHandler<T>) eventHandler).execute(event);
            }
        }

        return event;
    }

    /**
     * Registers an inline event handler for quick-and-easy listening to events.
     * This method of listening to events is not afforded the same customisability as handlers registered within
     * listeners.
     *
     * @param type     the class type of any object to listen to when an event is called
     * @param handler  what to do with that called object
     */
    public <T> long on(Class<T> type, EventHandler<T> handler) {
        List<EventHandler<?>> handlers = registeredHandlers.computeIfAbsent(type, t -> new LinkedList<>());

        handlers.add(handler);
        handlers.sort(handlerPriority);

        long index = handlerCounter++;
        indexedHandlers.put(index, handler);

        return index;
    }

    /**
     * Registers any class containing @Handler annotations.
     * @param listener  the listener
     */
    public Map<Long, EventHandler<?>> register(Object listener) {
        List<Method> methods = new ArrayList<>(List.of(listener.getClass().getDeclaredMethods()));

        Map<Long, EventHandler<?>> result = new HashMap<>();

        for (Method method : methods) {
            if (isHandler(method)) {
                Class<?> type = method.getParameterTypes()[0];
                List<EventHandler<?>> handlers = registeredHandlers.computeIfAbsent(type, l -> new LinkedList<>());
                EventHandler<Object> eventHandler = new EventHandler<>() {
                    @Override
                    public void execute(Object event) {
                        Object[] objects = new Object[method.getParameterCount()];
                        objects[0] = event;

                        for (int i = 1; i < method.getParameterCount(); i++) {
                            ProviderDefinition key = new ProviderDefinition(event.getClass(), method.getParameters()[i].getType());
                            Function<Object, Object> function = providedObjects.get(key);
                            if (function != null) {
                                objects[i] = function.apply(event);
                            }
                        }

                        try {
                            method.setAccessible(true);
                            method.invoke(listener, objects);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public int priority() {
                        return getHandlerPriority(method);
                    }
                };
                handlers.add(eventHandler);
                result.put(handlerCounter++, eventHandler);
                handlers.sort(handlerPriority);
            }
        }

        indexedHandlers.putAll(result);

        return result;
    }

    /**
     * Allows additional arguments to be provided which can be used in Listener event Handlers.
     *
     * @param callingType   the event object type
     * @param providedType  the provided object type
     * @param function      a function which takes the event object type and provides the provided object type
     */
    public <T, U> void provide(Class<T> callingType, Class<U> providedType, Function<T, U> function) {
        providedObjects.put(new ProviderDefinition(callingType, providedType), (Function<Object, Object>) function);
    }

    public void unregister(long index) {
        EventHandler<?> handler = indexedHandlers.remove(index);

        if (handler != null) {
            for (List<EventHandler<?>> handlers : registeredHandlers.values()) {
                handlers.remove(handler);
            }

            registeredHandlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    public void unregister(Class<?> type) {
        List<EventHandler<?>> remove = registeredHandlers.remove(type);

        if (remove != null) {
            for (EventHandler<?> handler : remove) {
                indexedHandlers.entrySet().removeIf(entry -> entry.getValue().equals(handler));
            }
        }

    }

    public void unregisterAll() {
        registeredHandlers.clear();
        indexedHandlers.clear();
    }

    private boolean isHandler(Method method) {
        return method.getAnnotation(Handler.class) != null && method.getParameterCount() != 0;
    }

    private static int getHandlerPriority(EventHandler<?> handler) {
        int priority = handler.priority();

        try {
            if (handler.getClass().getAnnotation(Handler.class) != null) {
                priority = handler.getClass().getAnnotation(Handler.class).priority();
            } else {
                priority = handler.getClass().getMethod("execute", Object.class).getAnnotation(Handler.class).priority();
            }
        } catch (Exception ignored) {
        }

        return priority;
    }

    private static int getHandlerPriority(Method method) {
        Handler annotation = method.getAnnotation(Handler.class);
        return annotation != null ? annotation.priority() : 0;
    }

    private static final class HandlerPriorityComparator implements Comparator<EventHandler<?>> {
        @Override
        public int compare(EventHandler<?> one, EventHandler<?> two) {
            return getHandlerPriority(one) - getHandlerPriority(two);
        }
    }

    private record ProviderDefinition(Class<?> callingType, Class<?> providedType) {
    }
}