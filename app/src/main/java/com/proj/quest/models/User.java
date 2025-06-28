package com.proj.quest.models;

import java.util.Date;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;
    @SerializedName("Логин")
    private String login;
    @SerializedName("Почта")
    private String email;
    @SerializedName("КоличествоБаллов")
    private int score;
    @SerializedName("ДатаРегистрации")
    private String registrationDate;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("teamId")
    private Integer teamId;

    public User() {}

    // Геттеры
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getEmail() { return email; }
    public int getScore() { return score; }
    public String getRegistrationDate() { return registrationDate; }
    public String getAvatarUrl() { return avatarUrl; }
    public Integer getTeamId() { return teamId; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setLogin(String login) { this.login = login; }
    public void setEmail(String email) { this.email = email; }
    public void setScore(int score) { this.score = score; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
}
