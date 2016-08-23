package com.dreamburst.sed.model;

import com.dreamburst.sed.dispatchers.Dispatchers;
import com.dreamburst.sed.events.NameChangeEvent;

public class Foo {
    private String name;
    private NameChangeEvent nameChangeEvent;

    public Foo(String name) {
        this.name = name;
        nameChangeEvent = new NameChangeEvent(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        nameChangeEvent.setName(name);

        if (!Dispatchers.dispatch(nameChangeEvent).isCancelled()) {
            this.name = nameChangeEvent.getName();
        }
    }
}