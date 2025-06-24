package com.proj.quest.models;

public class RegistrationRequest {
    private String login;
    private String email;
    private String password;

    public RegistrationRequest(String login, String email, String password) {
        this.login = login;
        this.email = email;
        this.password = password;
    }

    // Геттеры
    public String getLogin() { return login; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
