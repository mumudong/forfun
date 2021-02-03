package jmxdemo.subject_delegation;

import javax.management.Notification;
import javax.management.NotificationListener;

public class ClientListener implements NotificationListener {
    public void handleNotification(Notification notification, Object handback) {
        System.out.println("\nReceived notification: " + notification);
    }
}
