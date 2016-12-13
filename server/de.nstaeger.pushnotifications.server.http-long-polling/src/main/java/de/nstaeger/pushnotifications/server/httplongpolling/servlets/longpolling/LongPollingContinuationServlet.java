package de.nstaeger.pushnotifications.server.httplongpolling.servlets.longpolling;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.http.HttpStatus;

import de.nstaeger.pushnotifications.server.httplongpolling.notification.Notification;
import de.nstaeger.pushnotifications.server.httplongpolling.notification.NotificationService;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
@SuppressWarnings("serial")
public class LongPollingContinuationServlet extends HttpServlet
{
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
        List<Notification> missedNotifications = notificationService.getOlderNotificationsGreaterThan(lastId);

        if (!missedNotifications.isEmpty())
        {
            sendMissedNotifications(continuation, missedNotifications);
        }
        else
        {
            notificationService.registerEmitter(new LongPollingEmitter(continuation, lastId));
        }
    }

    private void sendMissedNotifications(Continuation continuation, List<Notification> missedNotifications)
        throws IOException
    {
        continuation.getServletResponse()
                    .getOutputStream()
                    .write(missedNotifications.toString().getBytes(UTF_8.name()));
        
        continuation.complete();
    }

    private Continuation createContinuationAndSuspend(HttpServletRequest request, HttpServletResponse response)
    {
        response.setStatus(HttpStatus.OK_200);
        response.setContentType("application/json");
        response.setCharacterEncoding(UTF_8.name());

        Continuation continuation = ContinuationSupport.getContinuation(request);
        continuation.setTimeout(0L);
        continuation.suspend(response);

        return continuation;
    }

    private int getLastIdFromRequest(final HttpServletRequest request)
    {
        final String lastIdAsString = request.getParameter("last");

        return lastIdAsString == null ? 0 : Integer.parseInt(lastIdAsString);
    }
}
