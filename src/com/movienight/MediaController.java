package com.movienight;

import com.movienight.MediaCommand;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class MediaController {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private NetworkManager networkManager;
    private boolean isHost;
    
    // Observable properties
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private final DoubleProperty currentTime = new SimpleDoubleProperty(0);
    private final DoubleProperty duration = new SimpleDoubleProperty(0);
    
    public MediaController(MediaView mediaView, NetworkManager networkManager, boolean isHost) {
        this.mediaView = mediaView;
        this.networkManager = networkManager;
        this.isHost = isHost;
        
        if (!isHost) {
            // Set up command handler for viewers
            networkManager.setMediaCommandHandler(this::handleMediaCommand);
        }
    }
    
    public void loadMedia(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        
        // Set up properties
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> 
            currentTime.set(newTime.toSeconds()));
            
        mediaPlayer.setOnReady(() -> {
            duration.set(mediaPlayer.getTotalDuration().toSeconds());
            
            // If host, broadcast initial state
            if (isHost) {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        Platform.runLater(this::broadcastMediaState);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
        
        mediaPlayer.setOnPlaying(() -> isPlaying.set(true));
        mediaPlayer.setOnPaused(() -> isPlaying.set(false));
        mediaPlayer.setOnStopped(() -> isPlaying.set(false));
        mediaPlayer.setOnEndOfMedia(() -> isPlaying.set(false));
    }
    
    // Host controls
    public void play() {
        if (mediaPlayer != null && isHost) {
            mediaPlayer.play();
            broadcastMediaCommand(MediaCommand.Type.PLAY, null);
        }
    }
    
    public void pause() {
        if (mediaPlayer != null && isHost) {
            mediaPlayer.pause();
            broadcastMediaCommand(MediaCommand.Type.PAUSE, null);
        }
    }
    
    public void seek(Duration duration) {
        if (mediaPlayer != null && isHost) {
            mediaPlayer.seek(duration);
            broadcastMediaCommand(MediaCommand.Type.SEEK, duration.toSeconds());
        }
    }
    
    public void stop() {
        if (mediaPlayer != null && isHost) {
            mediaPlayer.stop();
            broadcastMediaCommand(MediaCommand.Type.STOP, null);
        }
    }
    
    // Send media state to all viewers
    private void broadcastMediaState() {
        if (mediaPlayer != null && isHost) {
            // First, send the media information
            broadcastMediaCommand(MediaCommand.Type.LOAD, mediaPlayer.getMedia().getSource());
            
            // Short delay to ensure LOAD command is processed
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // Ignore
            }
            
            // Then send current state
            if (isPlaying.get()) {
                broadcastMediaCommand(MediaCommand.Type.PLAY, null);
            } else {
                broadcastMediaCommand(MediaCommand.Type.PAUSE, null);
            }
            
            // Another short delay
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // Ignore
            }
            
            // Send current position
            broadcastMediaCommand(MediaCommand.Type.SEEK, mediaPlayer.getCurrentTime().toSeconds());
        }
    }
    
    private void broadcastMediaCommand(MediaCommand.Type type, Object data) {
        if (isHost && networkManager != null) {
            MediaCommand command = new MediaCommand(type, data);
            networkManager.broadcastMediaCommand(command);
        }
    }
    
    // Handle incoming commands from host (for viewers)
    private void handleMediaCommand(MediaCommand command) {
        if (mediaPlayer == null && command.getType() != MediaCommand.Type.LOAD) {
            System.out.println("Received command but no media player exists yet: " + command.getType());
            return;
        }
        
        try {
            switch (command.getType()) {
                case LOAD:
                    String mediaSource = (String) command.getData();
                    System.out.println("Loading media from: " + mediaSource);
                    
                    Media media = new Media(mediaSource);
                    if (mediaPlayer != null) {
                        mediaPlayer.dispose();
                    }
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);
                    
                    // Set up the same properties as in loadMedia
                    mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> 
                        currentTime.set(newTime.toSeconds()));
                    
                    mediaPlayer.setOnReady(() -> {
                        duration.set(mediaPlayer.getTotalDuration().toSeconds());
                    });
                    
                    mediaPlayer.setOnPlaying(() -> isPlaying.set(true));
                    mediaPlayer.setOnPaused(() -> isPlaying.set(false));
                    mediaPlayer.setOnStopped(() -> isPlaying.set(false));
                    mediaPlayer.setOnEndOfMedia(() -> isPlaying.set(false));
                    break;
                    
                case PLAY:
                    System.out.println("Playing media");
                    mediaPlayer.play();
                    break;
                    
                case PAUSE:
                    System.out.println("Pausing media");
                    mediaPlayer.pause();
                    break;
                    
                case STOP:
                    System.out.println("Stopping media");
                    mediaPlayer.stop();
                    break;
                    
                case SEEK:
                    Double seconds = (Double) command.getData();
                    System.out.println("Seeking to: " + seconds);
                    mediaPlayer.seek(Duration.seconds(seconds));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling media command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public BooleanProperty isPlayingProperty() {
        return isPlaying;
    }
    
    public DoubleProperty currentTimeProperty() {
        return currentTime;
    }
    
    public DoubleProperty durationProperty() {
        return duration;
    }
    
    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}