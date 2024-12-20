package com.example.sciquest.Model;

public class Comment {
    private String comment;
    private String publisher;
    private long timestamp;
    private String postPicture;
    private long likeCount;

    public Comment() {}

    public Comment(String comment, String publisher, long timestamp) {
        this.comment = comment;
        this.publisher = publisher;
        this.timestamp = timestamp;
    }

    public  String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostPicture() {
        return postPicture;
    }

    public void setPostPicture(String postPicture) {
        this.postPicture = postPicture;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
