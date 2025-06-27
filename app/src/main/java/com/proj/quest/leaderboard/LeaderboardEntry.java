package com.proj.quest.leaderboard;

public class LeaderboardEntry {
    private String name;
    private int score;
    private String avatarUrl;

    public LeaderboardEntry(String name, int score, String avatarUrl) {
        this.name = name;
        this.score = score;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // Старый конструктор для обратной совместимости
    public LeaderboardEntry(String name, int score) {
        this(name, score, null);
    }
}