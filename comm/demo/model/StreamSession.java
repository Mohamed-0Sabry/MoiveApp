package comm.demo.model;

public class StreamSession {
    private boolean isScreenSharing;
    private boolean isCameraSharing;
    private boolean isStreaming;
    private String currentMediaPath;

    public StreamSession() {
        this.isScreenSharing = false;
        this.isCameraSharing = false;
        this.isStreaming = false;
        this.currentMediaPath = null;
    }

    public void startScreenShare() {
        this.isScreenSharing = true;
        this.isStreaming = true;
    }

    public void stopScreenShare() {
        this.isScreenSharing = false;
        this.isStreaming = false;
    }

    public void startCameraShare() {
        this.isCameraSharing = true;
        this.isStreaming = true;
    }

    public void stopCameraShare() {
        this.isCameraSharing = false;
        this.isStreaming = false;
    }

    public void startMediaStream(String mediaPath) {
        this.currentMediaPath = mediaPath;
        this.isStreaming = true;
    }

    public void stopStreaming() {
        this.isScreenSharing = false;
        this.isCameraSharing = false;
        this.isStreaming = false;
        this.currentMediaPath = null;
    }

    public boolean isScreenSharing() {
        return isScreenSharing;
    }

    public boolean isCameraSharing() {
        return isCameraSharing;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public String getCurrentMediaPath() {
        return currentMediaPath;
    }
} 