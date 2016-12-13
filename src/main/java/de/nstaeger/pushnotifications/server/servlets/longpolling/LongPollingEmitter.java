package de.nstaeger.pushnotifications.server.servlets.longpolling;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import javax.servlet.ServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nstaeger.pushnotifications.server.notification.Notification;
import de.nstaeger.pushnotifications.server.notification.NotificationEmitter;

public class LongPollingEmitter implements NotificationEmitter
{
    private static final Logger LOG = LoggerFactory.getLogger(LongPollingEmitter.class);

    private Continuation continuation;
    private int lastId;

    public LongPollingEmitter(final Continuation continuation, final int lastId)
    {
        this.continuation = continuation;
        this.lastId = lastId;
    }

    @Override
    public boolean emitNotification(Notification notification)
    {
        if (notification.getId() <= lastId)
        {
            return false;
        }

        writeResponse(continuation.getServletResponse(), notification);
        continuation.complete();

        return true;
    }

    private void writeResponse(ServletResponse servletResponse, Notification notification)
    {
        try
        {
            continuation.getServletResponse()
                        .getOutputStream()
                        .write(("[" + notification.toString() + "]").getBytes(UTF_8.name()));
        }
        catch (IOException e)
        {
            LOG.error("Could not write response", e);
        }
    }
}
