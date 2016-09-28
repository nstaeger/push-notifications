package de.nstaeger.pushnotifications.server.httplongpolling.notification;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
public class FakeNotificationService
{
    private static class Notifier implements Runnable
    {
        private final Runnable notificationSender;

        public Notifier(final Runnable notificationSender)
        {
            this.notificationSender = notificationSender;
        }

        @Override
        public void run()
        {
            final Random random = new Random();

            while (true)
            {
                try
                {
                    final int timeToWait = random.nextInt(5000) + 1000;
                    LOGGER.info("Next notification will be send in {}ms", timeToWait);

                    Thread.sleep(timeToWait);

                    notificationSender.run();
                }
                catch (final InterruptedException e)
                {
                    LOGGER.error("Notifier was interrupted!");
                    Thread.interrupted();
                }
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeNotificationService.class);
    private static final Object BLOCKER = new Object();
    private static final int NUMBER_OF_CACHED_NOTIFICATIONS = 5;
    private static int waitingClients = 0;

    private Thread notificationThread;
    private final Queue<Notification> notificationQueue;

    public FakeNotificationService()
    {
        notificationQueue = new CircularFifoQueue<>(NUMBER_OF_CACHED_NOTIFICATIONS);
    }

    public void start()
    {
        if (notificationThread == null)
        {
            notificationThread = new Thread(new Notifier(() -> sendNotification(new Notification("A notification"))));
            notificationThread.start();
        }
    }

    public List<Notification> waitForNotificationsGreaterThan(final int lastNotificationId, final int timeout)
    {
        incrementWaitingClients();

        try
        {
            final long waitingStart = System.currentTimeMillis();

            do
            {
                final List<Notification> newNotifications = findNotificationsNewerThan(lastNotificationId);

                if (!newNotifications.isEmpty())
                {
                    return newNotifications;
                }

                synchronized (BLOCKER)
                {
                    BLOCKER.wait(timeout - (System.currentTimeMillis() - waitingStart));
                }
            }
            while (System.currentTimeMillis() - waitingStart < timeout);

            return Collections.emptyList();
        }
        catch (final InterruptedException e)
        {
            return Collections.emptyList();
        }
        finally
        {
            decrementWaitingClients();
        }
    }

    private synchronized void decrementWaitingClients()
    {
        --waitingClients;
    }

    private List<Notification> findNotificationsNewerThan(final int lastNotificationId)
    {
        return notificationQueue.stream()
                                .filter((notification) -> notification.getId() > lastNotificationId)
                                .collect(Collectors.toList());
    }

    private synchronized void incrementWaitingClients()
    {
        ++waitingClients;
    }

    private void sendNotification(final Notification notification)
    {
        notificationQueue.add(notification);

        LOGGER.info("Notification {} send to {} waiting client(s)", notification, waitingClients);

        synchronized (BLOCKER)
        {
            BLOCKER.notifyAll();
        }
    }
}
