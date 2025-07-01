package com.proj.quest.models;

import com.google.gson.annotations.SerializedName;

public class EventProgress {
    @SerializedName("teamId")
    private int teamId;
    
    @SerializedName("eventId")
    private int eventId;
    
    @SerializedName("startTime")
    private String startTime;
    
    @SerializedName("completionTime")
    private String completionTime;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("completionTimeSeconds")
    private Double completionTimeSeconds;
    
    @SerializedName("totalRiddles")
    private int totalRiddles;
    
    @SerializedName("solvedRiddles")
    private int solvedRiddles;

    public EventProgress() {}

    // Геттеры
    public int getTeamId() { return teamId; }
    public int getEventId() { return eventId; }
    public String getStartTime() { return startTime; }
    public String getCompletionTime() { return completionTime; }
    public String getStatus() { return status; }
    public Double getCompletionTimeSeconds() { return completionTimeSeconds; }
    public int getTotalRiddles() { return totalRiddles; }
    public int getSolvedRiddles() { return solvedRiddles; }

    // Сеттеры
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setCompletionTime(String completionTime) { this.completionTime = completionTime; }
    public void setStatus(String status) { this.status = status; }
    public void setCompletionTimeSeconds(Double completionTimeSeconds) { this.completionTimeSeconds = completionTimeSeconds; }
    public void setTotalRiddles(int totalRiddles) { this.totalRiddles = totalRiddles; }
    public void setSolvedRiddles(int solvedRiddles) { this.solvedRiddles = solvedRiddles; }

    // Вспомогательные методы
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    public boolean isActive() {
        return "active".equals(status);
    }
    
    public String getFormattedCompletionTime() {
        if (completionTimeSeconds == null) return "Не завершено";
        
        long seconds = completionTimeSeconds.longValue();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
} 