package de.nstaeger.pushnotifications.server.httplongpolling.notification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService
{
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final List<NotificationEmitter> emiterList;

    public NotificationService()
    {
        emiterList = new LinkedList<>();
    }

    public void registerEmiter(NotificationEmitter notificationEmiter)
    {
        emiterList.add(notificationEmiter);
    }

    public void sendNotification(Notification notification)
    {
        LOG.info("Sending to {} clients notification {}", emiterList.size(), notification.toString());
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
