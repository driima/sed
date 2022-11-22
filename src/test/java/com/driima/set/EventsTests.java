package com.driima.set;

import com.driima.sed.EventHandler;
import com.driima.sed.Events;
import com.driima.sed.Handler;
import com.driima.sed.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventsTests {

    private static Events events;

    @BeforeAll
    public static void setup() {
        events = new Events();
    }

    @AfterEach
    public void tearDown() {
        events.unregisterAll();
    }

    @Test
    public void testSimpleEvents() {
        events.on(DataObject.class, dataObject -> dataObject.data = 1);

        events.register(new Listener() {
            @Handler
            public void onEvent(DataObject dataObject) {
                dataObject.data = 2;
            }
        });

        DataObject dataObject = events.call(new DataObject());

        assertEquals(2, dataObject.data);
    }

    @Test
    public void testPriorities() {
        events.on(DataObject.class, dataObject -> dataObject.data = 1);

        events.register(new Listener() {
            @Handler(priority = -1)
            public void onEvent(DataObject dataObject) {
                dataObject.data = 2;
            }
        });

        events.on(DataObject.class, new EventHandler<>() {
            @Override
            @Handler(priority = 1)
            public void execute(DataObject event) {
                event.data = 3;
            }
        });

        DataObject dataObject = events.call(new DataObject());

        assertEquals(3, dataObject.data);
    }

    @Test
    public void testProvidedParameters() {
        events.provide(DataObject.class, String.class, dataObject -> "" + dataObject.data);

        events.on(DataObject.class, dataObject -> dataObject.data = 1);

        events.register(new Listener() {
            @Handler(priority = 1)
            public void onEvent(DataObject dataObject, String dataAsString) {
                assertEquals("1", dataAsString);
            }
        });

        events.call(new DataObject());
    }

    private static final class DataObject {
        private int data;
    }
}
