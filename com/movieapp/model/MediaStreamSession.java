package com.movieapp.model;

public interface MediaStreamSession extends StreamSession {
    void startMediaStream(String mediaPath);
    String getCurrentMediaPath();
} 