package de.nstaeger.pushnotifications.server.httplongpolling.servlets.sse;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nstaeger.pushnotifications.server.httplongpolling.notification.NotificationService;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
@SuppressWarnings("serial")
public class ServerSentEventsServlet extends HttpServlet
{
    private static final String CONTENT_TYPE_EVENT_STREAM = "text/event-stream";
    private static final Logger LOG = LoggerFactory.getLogger(ServerSentEventsServlet.class);

    private final NotificationService notificationService;

    public ServerSentEventsServlet(final NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws IOException
    {
        if (!doesAcceptEventStream(request))
        {
            response.sendError(HttpStatus.NOT_ACCEPTABLE_406);

            LOG.info("User requested an SSE stream without correct Accept Header");

            return;
        }

        sendHeaders(response);
        Continuation continuation = createContinuationAndSuspend(request, response);
        notificationService.registerEmiter(new ServerSentEventEmitter(continuation));
    }

    private Continuation createContinuationAndSuspend(HttpServletRequest request,
                                                      HttpServletResponse response)
    {
        Continuation continuation = ContinuationSupport.getContinuation(request);
        continuation.setTimeout(0L);
        continuation.suspend(response);

        return continuation;
    }

    private boolean doesAcceptEventStream(HttpServletRequest request)
    {
        Enumeration<String> headers = request.getHeaders("Accept");

        while (headers.hasMoreElements())
        {
            if (headers.nextElement().contains(CONTENT_TYPE_EVENT_STREAM))
            {
                return true;
            }
        }

        return false;
    }

    private void sendHeaders(HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpStatus.OK_200);
        response.setContentType(CONTENT_TYPE_EVENT_STREAM);
        response.setCharacterEncoding(UTF_8.name());
        response.addHeader("Connection", "close");
        response.flushBuffer();
    }
}
