package com.movieapp.model;

public class StreamSessionImpl implements ScreenShareSession, CameraShareSession, MediaStreamSession {
    private boolean isScreenSharing;
    private boolean isCameraSharing;
    private boolean isStreaming;
    private String currentMediaPath;

    public StreamSessionImpl() {
        this.isScreenSharing = false;
        this.isCameraSharing = false;
        this.isStreaming = false;
        this.currentMediaPath = null;
    }

    @Override
    public void startScreenShare() {
        this.isScreenSharing = true;
        this.isStreaming = true;
    }

    @Override
    public void stopScreenShare() {
        this.isScreenSharing = false;
        this.isStreaming = false;
    }

    @Override
    public void startCameraShare() {
        this.isCameraSharing = true;
        this.isStreaming = true;
    }

    @Override
    public void stopCameraShare() {
        this.isCameraSharing = false;
        this.isStreaming = false;
    }

    @Override
    public void startMediaStream(String mediaPath) {
        this.currentMediaPath = mediaPath;
        this.isStreaming = true;
    }

    @Override
    public void stopStreaming() {
        this.isScreenSharing = false;
        this.isCameraSharing = false;
        this.isStreaming = false;
        this.currentMediaPath = null;
    }

    @Override
    public boolean isScreenSharing() {
        return isScreenSharing;
    }

    @Override
    public boolean isCameraSharing() {
        return isCameraSharing;
    }

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public String getCurrentMediaPath() {
        return currentMediaPath;
    }
} 