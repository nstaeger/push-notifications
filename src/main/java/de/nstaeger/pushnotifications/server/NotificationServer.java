package de.nstaeger.pushnotifications.server;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import de.nstaeger.pushnotifications.server.notification.NotificationService;
import de.nstaeger.pushnotifications.server.servlets.longpolling.LongPollingServlet;
import de.nstaeger.pushnotifications.server.servlets.sse.ServerSentEventsServlet;

/**
 * @author <a href="mail@nstaeger.de">Nicolai Stäger</a>
 */
public class NotificationServer extends Server
{
    private static final int THREAD_POOL_SIZE = 20000;
    
    public NotificationServer(final int port, final NotificationService notificationService)
    {
        super(new QueuedThreadPool(THREAD_POOL_SIZE));

        final ServerConnector connector = new ServerConnector(this);
        connector.setPort(port);
        addConnector(connector);

        // Servlet for Long Polling
        final HttpServlet longPollingServlet = new LongPollingServlet(notificationService);
        final ServletHolder longPollingServletHolder = new ServletHolder(longPollingServlet);
        longPollingServletHolder.setAsyncSupported(true);

        // Servlet for Server-Sent Events
        final HttpServlet sseServlet = new ServerSentEventsServlet(notificationService);
        final ServletHolder sseServletHolder = new ServletHolder(sseServlet);
        sseServletHolder.setAsyncSupported(true);

        final ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.addServlet(longPollingServletHolder, "/longpolling/*");
        contextHandler.addServlet(sseServletHolder, "/serversentevents/*");

        setHandler(contextHandler);
    }
}
