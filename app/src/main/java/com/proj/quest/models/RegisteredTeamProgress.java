package com.proj.quest.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class RegisteredTeamProgress implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("captainId")
    private int captainId;
    @SerializedName("members")
    private List<User> members;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("completionTime")
    private String completionTime;
    @SerializedName("status")
    private String status;
    @SerializedName("completionTimeSeconds")
    private Double completionTimeSeconds;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCaptainId() { return captainId; }
    public List<User> getMembers() { return members; }
    public String getStartTime() { return startTime; }
    public String getCompletionTime() { return completionTime; }
    public String getStatus() { return status; }
    public Double getCompletionTimeSeconds() { return completionTimeSeconds; }
} 