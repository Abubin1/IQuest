package com.proj.quest.models;

public class Riddle {
    private int id;
    private int eventId;
    private String text;
    private int landmarkId;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getLandmarkId() { return landmarkId; }
    public void setLandmarkId(int landmarkId) { this.landmarkId = landmarkId; }
}