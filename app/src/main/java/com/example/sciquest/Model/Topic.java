package com.example.sciquest.Model;

public class Topic {

    private String topic;
    private int imageResID;

    public Topic(String topic, int imageResID){
        this.topic = topic;
        this.imageResID = imageResID;
    }

    public String getTopic(){
        return this.topic;
    }

    public int getImageResID(){
        return this.imageResID;
    }

}
