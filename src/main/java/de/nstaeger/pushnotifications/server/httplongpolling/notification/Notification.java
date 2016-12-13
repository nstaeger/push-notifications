package de.nstaeger.pushnotifications.server.httplongpolling.notification;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:mail@nstaeger.com">Nicolai Stäger</a>
 */
public class Notification
{
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final int id;
    private final String message;

    public Notification(final String message)
    {
        id = COUNTER.incrementAndGet();
        this.message = message;
    }

    public int getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return String.format("{\"id\": %d, \"message\": \"%s\"}", id, message);
    }
}
