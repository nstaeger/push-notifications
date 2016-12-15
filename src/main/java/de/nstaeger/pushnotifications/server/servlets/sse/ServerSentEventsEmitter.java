package de.nstaeger.pushnotifications.server.servlets.sse;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;

import org.eclipse.jetty.continuation.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nstaeger.pushnotifications.server.notification.Notification;
import de.nstaeger.pushnotifications.server.notification.NotificationEmitter;
import de.nstaeger.pushnotifications.server.util.Executable;

class ServerSentEventsEmitter implements NotificationEmitter
{
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] DATA_FIELD;
    private static final Logger LOG = LoggerFactory.getLogger(ServerSentEventsEmitter.class);

    static
    {
        try
        {
            DATA_FIELD = "data: ".getBytes(UTF_8.name());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private final Continuation continuation;
    private final ServletOutputStream outputStream;
    private boolean closed;

    public ServerSentEventsEmitter(final Continuation continuation) throws IOException
    {
        this.continuation = continuation;
        outputStream = continuation.getServletResponse().getOutputStream();
        closed = false;
    }

    @Override
    public boolean emitNotification(Notification notification)
    {
        writeNotification(notification);

        return isClosed();
    }

    private synchronized void close()
    {
        closed = true;
        continuation.complete();
    }

    private void flush() throws IOException
    {
        continuation.getServletResponse().flushBuffer();
    }

    private boolean isClosed()
    {
        return closed;
    }

    private synchronized void writeNotification(Notification notification)
    {
        runAndCloseOnConnectionClosed(() -> {
            outputStream.write(DATA_FIELD);
            outputStream.write(notification.toString().getBytes(UTF_8.name()));
            outputStream.write(CRLF);
            outputStream.write(CRLF);
            flush();
        });
    }

    private void runAndCloseOnConnectionClosed(Executable executable)
    {
        try
        {
            executable.execute();
        }
        catch (IOException e)
        {
            LOG.info("Client closed connection");
            close();
        }
        catch (Exception e)
        {
            LOG.error("Could not execute the given callable successfully", e);
        }
    }
}
