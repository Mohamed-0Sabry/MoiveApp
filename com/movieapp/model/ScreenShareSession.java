package com.movieapp.model;

public interface ScreenShareSession extends StreamSession {
    void startScreenShare();
    void stopScreenShare();
    boolean isScreenSharing();
} 