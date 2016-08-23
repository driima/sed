package com.dreamburst.sed.tests;

import com.dreamburst.sed.Handler;
import com.dreamburst.sed.Listener;
import com.dreamburst.sed.dispatchers.BatchDispatcher;
import com.dreamburst.sed.dispatchers.Dispatchers;
import com.dreamburst.sed.events.NameChangeEvent;
import com.dreamburst.sed.model.Foo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BatchDispatcherTests {

    private static final BatchDispatcher dispatcher = Dispatchers.get(BatchDispatcher.class);

    @Test
    public void fooNameShouldBeBaz() {
        dispatcher.register(new NameChangeEventListener());

        Foo foo = new Foo("Foo");
        foo.setName("Bar");

        dispatcher.unregisterAll();

        assertEquals("Baz", foo.getName());
    }

    @Test
    public void fooNameShouldBeFoo() {
        dispatcher.register(new NameChangeCanceller());

        Foo foo = new Foo("Foo");
        foo.setName("Bar");

        dispatcher.unregisterAll();

        assertEquals("Foo", foo.getName());
    }

    public static class NameChangeEventListener implements Listener {

        @Handler
        public void fooNameToBaz(NameChangeEvent event) {
            event.setName("Baz");
        }

        @Handler(priority = -1)
        public void fooNameToLoremIpsum(NameChangeEvent event) {
            event.setName("Lorem Ipsum");
        }
    }

    public static class NameChangeCanceller implements Listener {

        @Handler
        public void fooNameChange(NameChangeEvent event) {
            event.setCancelled(true);
        }
    }
}
