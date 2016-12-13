package de.nstaeger.pushnotifications.server.httplongpolling.notification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService
{
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
    private static final int NUMBER_OF_CACHED_NOTIFICATIONS = 5;

    private final List<NotificationEmitter> emiterList;
    private final Queue<Notification> notificationQueue;

    public NotificationService()
    {
        emiterList = new LinkedList<>();
        notificationQueue = new CircularFifoQueue<>(NUMBER_OF_CACHED_NOTIFICATIONS);
    }

    public List<Notification> getOlderNotificationsGreaterThan(int lastNotificationId)
    {
        return notificationQueue.stream()
                                .filter((notification) -> notification.getId() > lastNotificationId)
                                .collect(Collectors.toList());
    }

    public void registerEmitter(NotificationEmitter notificationEmiter)
    {
        emiterList.add(notificationEmiter);
    }

    public void sendNotification(Notification notification)
    {
        LOG.info("Sending to {} clients notification {}", emiterList.size(), notification.toString());
        notificationQueue.add(notification);
        notifyEmiter(notification);
    }

    private void notifyEmiter(Notification notification)
    {
        Iterator<NotificationEmitter> i = emiterList.iterator();

        while (i.hasNext())
        {
            NotificationEmitter emiter = i.next();
            boolean shouldBeRemoved = emiter.emitNotification(notification);

            if (shouldBeRemoved)
            {
                i.remove();
            }
        }
    }
}
