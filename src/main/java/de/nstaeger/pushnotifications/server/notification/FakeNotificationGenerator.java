package de.nstaeger.pushnotifications.server.notification;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mail@nstaeger.de">Nicolai Stäger</a>
 */
public class FakeNotificationGenerator
{
    private class NotificationSender implements Runnable
    {
        @Override
        public void run()
        {
            Notification notification = new Notification("Test Notification");
            notificationService.sendNotification(notification);
            scheduleNotificaiton();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(FakeNotificationGenerator.class);

    private NotificationService notificationService;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledNotification;

    public FakeNotificationGenerator(NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }

    public synchronized void start()
    {
        if (scheduler == null)
        {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduleNotificaiton();
        }
    }

    public synchronized void stop()
    {
        if (scheduledNotification != null)
        {
            scheduledNotification.cancel(false);
            scheduledNotification = null;
        }

        if (scheduler != null)
        {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void scheduleNotificaiton()
    {
        if (scheduler != null)
        {
            final int timeToWait = new Random().nextInt(5000) + 1000;
            LOG.info("Next notification will be send in {}ms", timeToWait);
            
            scheduledNotification = scheduler.schedule(new NotificationSender(),
                                                       timeToWait,
                                                       TimeUnit.MILLISECONDS);
        }
    }
}
