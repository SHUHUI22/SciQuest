package com.example.sciquest.Model;


import com.google.firebase.Timestamp;

public class Notification {
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp timestamp;
    private int imageResID;
    private String notificationId;

    public Notification() {
    }

    // Constructor
    public Notification(String title, String message, boolean isRead, Timestamp timestamp, int imageResID) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.imageResID = imageResID;
    }

    // Getter and Setter methods
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getImageResID(){
        return imageResID;
    }
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

}
