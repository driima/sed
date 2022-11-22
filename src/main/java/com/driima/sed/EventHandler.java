package com.driima.sed;

public interface EventHandler<T> {
    void execute(T event);

    default int priority() {
        return 0;
    }
}
