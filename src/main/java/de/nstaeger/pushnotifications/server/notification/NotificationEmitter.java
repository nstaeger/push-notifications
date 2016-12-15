package de.nstaeger.pushnotifications.server.notification;

public interface NotificationEmitter
{
    /**
     * @return whether to unregister this emitter from the service after this notification or not.
     */
    boolean emitNotification(Notification notification);
}
