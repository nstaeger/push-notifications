package de.nstaeger.pushnotifications.server;

import de.nstaeger.pushnotifications.server.notification.FakeNotificationGenerator;
import de.nstaeger.pushnotifications.server.notification.NotificationService;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
public class Application
{
    private static final int PORT = 8080;

    public static void main(final String[] args) throws Exception
    {
        final NotificationService notificationService = new NotificationService();
        FakeNotificationGenerator notificationGenerator = new FakeNotificationGenerator(notificationService);
        final NotificationServer server = new NotificationServer(PORT, notificationService);

        notificationGenerator.start();
        server.start();
        server.join();
    }
}
