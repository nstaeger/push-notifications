package de.nstaeger.pushnotifications.server.httplongpolling;

import java.io.IOException;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nstaeger.pushnotifications.server.httplongpolling.notification.FakeNotificationService;
import de.nstaeger.pushnotifications.server.httplongpolling.notification.Notification;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
@SuppressWarnings("serial")
public class EventsServlet extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventsServlet.class);
    private static final int DEFAULT_TIMEOUT = 3000;

    private final FakeNotificationService notificationService;

    public EventsServlet(final FakeNotificationService notificationService)
    {
        this.notificationService = notificationService;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        final AsyncContext asyncContext = request.startAsync();

        asyncContext.start(() ->
        {
            try
            {
                respondWithNotifications(request, response);
            }
            catch (final Exception e)
            {
                LOGGER.error("Trying to respond with Notifications throws excpetion.", e);
            }
            finally
            {
                asyncContext.complete();
            }
        });
    }

    private int getLastIdFromRequest(final HttpServletRequest request)
    {
        final String lastIdAsString = request.getParameter("last");

        return lastIdAsString == null ? 0 : Integer.parseInt(lastIdAsString);
    }

    private void respondWithNotifications(final HttpServletRequest request, final HttpServletResponse response)
        throws IOException
    {
        final int lastId = getLastIdFromRequest(request);

        LOGGER.debug("Client requesting notifications after {}", lastId);

        final List<Notification> notifications = notificationService.waitForNotificationsGreaterThan(lastId,
                                                                                                     DEFAULT_TIMEOUT);

        if (notifications.isEmpty())
        {
            response.setStatus(HttpStatus.NO_CONTENT_204);
        }
        else
        {
            response.setContentType("application/json");
            response.getWriter().write(notifications.toString());
        }

        LOGGER.debug("Client gets {} notification(s)", notifications.size());
    }
}
