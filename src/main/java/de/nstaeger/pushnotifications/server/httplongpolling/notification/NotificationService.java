package de.nstaeger.pushnotifications.server.httplongpolling.notification;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService
{
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
    private static final int NUMBER_OF_CACHED_NOTIFICATIONS = 5;

    private final CopyOnWriteArrayList<NotificationEmitter> emitterList;
    private final Queue<Notification> notificationQueue;

    public NotificationService()
    {
        emitterList = new CopyOnWriteArrayList<>();
        notificationQueue = new CircularFifoQueue<>(NUMBER_OF_CACHED_NOTIFICATIONS);
    }

    public List<Notification> getOlderNotificationsGreaterThan(final int lastNotificationId)
    {
        return notificationQueue.stream()
                                .filter((notification) -> notification.getId() > lastNotificationId)
                                .collect(Collectors.toList());
    }

    public void registerEmitter(final NotificationEmitter notificationEmiter)
    {
        emitterList.add(notificationEmiter);
    }

    public void sendNotification(final Notification notification)
    {
        LOG.info("Sending to {} clients notification {}", emitterList.size(), notification.toString());
        notificationQueue.add(notification);
        notifyEmitter(notification);
        LOG.info("Notification send");
    }

    private void notifyEmitter(final Notification notification)
    {
        final Iterator<NotificationEmitter> i = emitterList.listIterator();

        while (i.hasNext())
        {
            final NotificationEmitter emitter = i.next();
            final boolean shouldBeRemoved = emitter.emitNotification(notification);

            if (shouldBeRemoved)
            {
                emitterList.remove(emitter);
            }
        }
    }
}
