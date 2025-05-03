package com.movieapp.model;

public interface CameraShareSession extends StreamSession {
    void startCameraShare();
    void stopCameraShare();
    boolean isCameraSharing();
} 