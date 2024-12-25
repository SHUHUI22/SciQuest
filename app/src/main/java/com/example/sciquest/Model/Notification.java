package com.example.sciquest.Model;

public class Notification {
    private String title;
    private String message;
    private boolean isRead;
    private String timestamp;
    private int imageResID;

    // Constructor
    public Notification(String title, String message, boolean isRead, String timestamp) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public int getImageResID(){
        return imageResID;
    }
}
