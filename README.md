# SED - Simple Event Dispatcher

SED is a simple Java event dispatcher which dispatches events that are handled by defined Listeners and EventHandlers.

## Usage

There are two types of dispatchers built into SED. Consider the following class setup:

#### NameChangeEvent

```java
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

```

#### Foo

```java
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
        
        if (!Dispatchers.dispatch(nameChangeEvent, DirectDispatcher.class).isCancelled()) {
            this.name = nameChangeEvent.getName();
        }
    }
}
```

### Using DirectDispatcher

`DirectDispatcher` allows event handlers to be directly registered to an event type and executed when the event is called:

```java
DirectDispatcher dispatcher = Dispatchers.get(DirectDispatcher.class);

Foo foo = new Foo("Foo");

dispatcher.register(NameChangeEvent.class, event -> event.setName("Baz"));
dispatcher.register(NameChangeEvent.class, new EventHandler<NameChangeEvent>() {
    @Override
    @Handler(priority = -1)
    public void execute(NameChangeEvent event) {
        event.setName("Lorem Ipsum");
    }
});

foo.setName("Bar");

System.out.println(foo.getName()); // "Baz"
```

### Using BatchDispatcher

`BatchDispatcher` allows listeners to be registered. Listeners contain handlers for any number of different events:

```java
BatchDispatcher dispatcher = Dispatchers.get(BatchDispatcher.class);

Foo foo = new Foo("Foo");

dispatcher.register(new NameChangeEventListener());

foo.setName("Bar");

System.out.println(foo.getName()); // "Baz"
```

##### NameChangeEventListener

```java
public class NameChangeEventListener implements Listener {

    @Handler
    public void fooNameToBaz(NameChangeEvent event) {
        event.setName("Baz");
    }

    @Handler(priority = -1)
    public void fooNameToLoremIpsum(NameChangeEvent event) {
        event.setName("Lorem Ipsum");
    }
}
```

## Priority

Event handlers are executed based on their priority, where lower numbers are executed first. Supplying `@Handler(priority = 1)` on a direct event handler's class declaration or `execute` method, or on the event handler methods within a `Listener`, means that it will always be executed after any handlers with a priority < 1. By default, priority is 0, so event handlers will be executed based on insertion order, but this cannot always be guaranteed.

## Invoking dispatchers

 A dispatcher can be created and used in a static context, but the `Dispatchers` class can simplify this process by storing the dispatchers for use when needed.

#### Using `Dispatchers`

```java
DirectDispatcher dispatcher = Dispatchers.get(DirectDispatcher.class);
```

#### Without using `Dispatchers`

```java
public class GlobalDirectDispatcher {

    private static final DirectDispatcher dispatcher = new DirectDispatcher();
    
    public static DirectDispatcher getDispatcher() {
        return dispatcher;
    }
}
```