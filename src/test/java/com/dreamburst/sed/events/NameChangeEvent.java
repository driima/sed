/*
  Copyright 2016 Dreamburst
 */

package com.dreamburst.sed.events;

import com.dreamburst.sed.Cancellable;
import com.dreamburst.sed.Event;

public final class NameChangeEvent implements Event, Cancellable {

    private String name;
    private boolean cancelled;

    public NameChangeEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
