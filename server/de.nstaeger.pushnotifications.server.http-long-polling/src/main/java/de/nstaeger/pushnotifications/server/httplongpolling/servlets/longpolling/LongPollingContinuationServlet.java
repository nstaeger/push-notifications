package de.nstaeger.pushnotifications.server.httplongpolling.servlets.longpolling;

import java.io.IOException;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nstaeger.pushnotifications.server.httplongpolling.notification.Notification;
import de.nstaeger.pushnotifications.server.httplongpolling.notification.NotificationEmitter;
import de.nstaeger.pushnotifications.server.httplongpolling.notification.NotificationService;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
@SuppressWarnings("serial")
public class LongPollingContinuationServlet extends HttpServlet
{
    private class LongPollingEmiter implements NotificationEmitter
    {
        private Continuation continuation;
        private int lastId;

        public LongPollingEmiter(final Continuation continuation, final int lastId)
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
                ServletResponse response = continuation.getServletResponse();
                response.setContentType("application/json");
                response.getOutputStream().write(notification.toString().getBytes());
            }
            catch (IOException e)
            {
                LOG.error("Could not write response", e);
            }
        }
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(LongPollingContinuationServlet.class);

    private final NotificationService notificationService;

    public LongPollingContinuationServlet(final NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws IOException
    {
        final int lastId = getLastIdFromRequest(request);
        Continuation continuation = createContinuationAndSuspend(request, response);

        notificationService.registerEmiter(new LongPollingEmiter(continuation, lastId));
    }

    private int getLastIdFromRequest(final HttpServletRequest request)
    {
        final String lastIdAsString = request.getParameter("last");

        return lastIdAsString == null ? 0 : Integer.parseInt(lastIdAsString);
    }

    private Continuation createContinuationAndSuspend(HttpServletRequest request,
                                                      HttpServletResponse response)
    {
        Continuation continuation = ContinuationSupport.getContinuation(request);
        continuation.setTimeout(0L);
        continuation.suspend(response);

        return continuation;
    }
}
