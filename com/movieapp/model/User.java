package com.movieapp.model;

import java.net.Socket;

public class User {
    private String username;
    private final boolean isHost;
    private Socket socket;
    private int audioPort;

    public User(String username, boolean isHost, Socket socket) {
        this.username = username;
        this.isHost = isHost;
        this.socket = socket;
    }

    public int getAudioPort() {
        return audioPort;
    }

    public void setAudioPort(int p) {
        this.audioPort = p;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isHost() {
        return isHost;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}