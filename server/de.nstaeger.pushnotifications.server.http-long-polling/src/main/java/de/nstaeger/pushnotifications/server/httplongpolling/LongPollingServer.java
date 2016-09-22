package de.nstaeger.pushnotifications.server.httplongpolling;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import de.nstaeger.pushnotifications.server.httplongpolling.notification.FakeNotificationService;

/**
 * @author <a href="mail@nstaeger.de">Nicolai Stäger</a>
 */
public class LongPollingServer extends Server
{
    public LongPollingServer(final int port, final FakeNotificationService notificationService)
    {
        super(new QueuedThreadPool(10000));

        final ServerConnector connector = new ServerConnector(this);
        connector.setPort(port);
        addConnector(connector);

        final HttpServlet servlet = new EventsServlet(notificationService);

        final ServletHolder servletHolder = new ServletHolder(servlet);
        servletHolder.setAsyncSupported(true);

        final ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.addServlet(servletHolder, "/events/*");

        setHandler(contextHandler);
    }
}
