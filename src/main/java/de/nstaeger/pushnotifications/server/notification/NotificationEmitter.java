package de.nstaeger.pushnotifications.server.notification;

public interface NotificationEmitter
{
    /**
     * @return whether to deregister this emiter from the service or not.
     */
    boolean emitNotification(Notification notification);
}
