package de.nstaeger.pushnotifications.server.httplongpolling;

import de.nstaeger.pushnotifications.server.httplongpolling.notification.FakeNotificationService;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
public class App
{
    private static final int PORT = 8080;

    public static void main(final String[] args)
        throws Exception
    {
        final FakeNotificationService notificationService = new FakeNotificationService();
        final LongPollingServer server = new LongPollingServer(PORT, notificationService);

        notificationService.start();
        server.start();
        server.join();
    }
}
