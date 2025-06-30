package com.proj.quest.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RiddleRequest implements Serializable {
    @SerializedName("riddle_text")
    private String riddle_text;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    public RiddleRequest(String riddle_text, double latitude, double longitude) {
        this.riddle_text = riddle_text;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getRiddle_text() {
        return riddle_text;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Setters
    public void setRiddle_text(String riddle_text) {
        this.riddle_text = riddle_text;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
} 