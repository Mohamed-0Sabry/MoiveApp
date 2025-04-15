package com.movienight;

import com.movienight.MediaController;
import com.movienight.MovieNightApp;
import com.movienight.NetworkManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaView;

public class ViewerScreen extends BorderPane {
    private final MovieNightApp app;
    private final NetworkManager networkManager;
    private MediaController mediaController;
    
    public ViewerScreen(MovieNightApp app, NetworkManager networkManager) {
        this.app = app;
        this.networkManager = networkManager;
        
        // Set up the UI
        setupUI();
    }
    
    private void setupUI() {
        // Top - Header
        HBox headerBox = new HBox(10);
        headerBox.setPadding(new Insets(10));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Movie Night - Viewer Mode");
        titleLabel.getStyleClass().add("header-label");
        
        Label hostLabel = new Label("Connected to host: " + networkManager.hostAddressProperty().get());
        networkManager.hostAddressProperty().addListener((obs, oldVal, newVal) -> {
            hostLabel.setText("Connected to host: " + newVal);
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backButton = new Button("Back to Role Selection");
        backButton.setOnAction(e -> {
            if (mediaController != null) {
                mediaController.dispose();
            }
            app.showRoleSelectionScreen();
        });
        
        headerBox.getChildren().addAll(titleLabel, hostLabel, spacer, backButton);
        setTop(headerBox);
        
        // Center - Media Player
        MediaView mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        
        StackPane mediaPane = new StackPane(mediaView);
        mediaPane.setStyle("-fx-background-color: black;");
        
        // Initialize media controller (isHost = false)
        mediaController = new MediaController(mediaView, networkManager, false);
        
        // Make the MediaView resize with the window
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        
        setCenter(mediaPane);
        
        // Bottom - Info and status
        HBox statusBox = new HBox(10);
        statusBox.setPadding(new Insets(10));
        statusBox.setAlignment(Pos.CENTER);
        
        Label statusLabel = new Label("Waiting for host to start playback...");
        
        // Update status based on playback
        mediaController.isPlayingProperty().addListener((obs, wasPlaying, isPlaying) -> {
            if (isPlaying) {
                statusLabel.setText("Playing media from host");
            } else {
                statusLabel.setText("Playback paused");
            }
        });
        
        Slider timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setMax(100);
        timeSlider.setPrefWidth(300);
        timeSlider.setDisable(true); // Viewers can't control playback
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        
        // Update slider from media position (read-only)
        mediaController.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            double max = mediaController.durationProperty().get();
            if (max > 0) {
                timeSlider.setValue(newVal.doubleValue() / max * 100);
            }
        });
        
        Label timeLabel = new Label("00:00 / 00:00");
        
        // Update time label
        mediaController.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            updateTimeLabel(timeLabel, newVal.doubleValue(), mediaController.durationProperty().get());
        });
        
        mediaController.durationProperty().addListener((obs, oldVal, newVal) -> {
            updateTimeLabel(timeLabel, mediaController.currentTimeProperty().get(), newVal.doubleValue());
        });
        
        statusBox.getChildren().addAll(statusLabel, timeSlider, timeLabel);
        setBottom(statusBox);
    }
    
    private void updateTimeLabel(Label timeLabel, double currentSeconds, double totalSeconds) {
        timeLabel.setText(formatTime(currentSeconds) + " / " + formatTime(totalSeconds));
    }
    
    private String formatTime(double seconds) {
        int minutes = (int) (seconds / 60);
        int remainingSeconds = (int) (seconds % 60);
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}