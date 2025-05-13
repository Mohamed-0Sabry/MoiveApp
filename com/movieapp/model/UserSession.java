package com.movieapp.model;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String email;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void clear() {
        this.username = null;
        this.email = null;
    }
} 