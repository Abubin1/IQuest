package com.proj.quest.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CreateEventRequest implements Serializable {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("event_time")
    private String event_time;
    @SerializedName("max_participants")
    private int max_participants;
    @SerializedName("max_teams")
    private int max_teams;
    @SerializedName("number_of_riddles")
    private int number_of_riddles;
    @SerializedName("start_place")
    private String start_place;
    @SerializedName("theme_url")
    private String theme_url;
    @SerializedName("riddles")
    private List<RiddleRequest> riddles;

    public CreateEventRequest(String name, String description, String event_time, int max_participants, int number_of_riddles, String start_place, int max_teams) {
        this.name = name;
        this.description = description;
        this.event_time = event_time;
        this.max_participants = max_participants;
        this.number_of_riddles = number_of_riddles;
        this.start_place = start_place;
        this.max_teams = max_teams;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEvent_time() {
        return event_time;
    }

    public int getMax_participants() {
        return max_participants;
    }

    public int getMax_teams() {
        return max_teams;
    }

    public int getNumber_of_riddles() {
        return number_of_riddles;
    }

    public String getStart_place() {
        return start_place;
    }

    public String getTheme_url() {
        return theme_url;
    }

    public List<RiddleRequest> getRiddles() {
        return riddles;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }

    public void setMax_participants(int max_participants) {
        this.max_participants = max_participants;
    }

    public void setMax_teams(int max_teams) {
        this.max_teams = max_teams;
    }

    public void setNumber_of_riddles(int number_of_riddles) {
        this.number_of_riddles = number_of_riddles;
    }

    public void setStart_place(String start_place) {
        this.start_place = start_place;
    }

    public void setTheme_url(String theme_url) {
        this.theme_url = theme_url;
    }

    public void setRiddles(List<RiddleRequest> riddles) {
        this.riddles = riddles;
    }
} 