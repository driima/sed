package com.dreamburst.sed.tests;

import com.dreamburst.sed.EventHandler;
import com.dreamburst.sed.Handler;
import com.dreamburst.sed.dispatchers.DirectDispatcher;
import com.dreamburst.sed.dispatchers.Dispatchers;
import com.dreamburst.sed.events.NameChangeEvent;
import com.dreamburst.sed.model.Foo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DirectDispatcherTests {

    private static final DirectDispatcher dispatcher = Dispatchers.get(DirectDispatcher.class);

    @Test
    public void fooNameShouldBeBaz() {
        dispatcher.register(NameChangeEvent.class, event -> event.setName("Baz"));
        dispatcher.register(NameChangeEvent.class, new EventHandler<NameChangeEvent>() {
            @Override
            @Handler(priority = -1)
            public void execute(NameChangeEvent event) {
                event.setName("Lorem Ipsum");
            }
        });

        Foo foo = new Foo("Foo");
        foo.setName("Bar");

        dispatcher.unregisterAll();

        assertEquals("Baz", foo.getName());
    }

    @Test
    public void fooNameShouldBeFoo() {
        dispatcher.register(NameChangeEvent.class, event -> event.setCancelled(true));

        Foo foo = new Foo("Foo");
        foo.setName("Bar");

        dispatcher.unregisterAll();

        assertEquals("Foo", foo.getName());
    }
}
