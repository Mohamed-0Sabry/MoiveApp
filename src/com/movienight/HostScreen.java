package com.movienight;

import com.movienight.MediaController;
import com.movienight.MovieNightApp;
import com.movienight.NetworkManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class HostScreen extends BorderPane {
    private final MovieNightApp app;
    private final NetworkManager networkManager;
    private MediaController mediaController;
    private Label connectedClientsLabel;
    
    public HostScreen(MovieNightApp app, NetworkManager networkManager) {
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
        
        Label titleLabel = new Label("Movie Night - Host Mode");
        titleLabel.getStyleClass().add("header-label");
        
        connectedClientsLabel = new Label("Connected viewers: 0");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backButton = new Button("Back to Role Selection");
        backButton.setOnAction(e -> {
            if (mediaController != null) {
                mediaController.dispose();
            }
            app.showRoleSelectionScreen();
        });
        
        headerBox.getChildren().addAll(titleLabel, spacer, connectedClientsLabel, backButton);
        setTop(headerBox);
        
        // Center - Media Player
        MediaView mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        
        StackPane mediaPane = new StackPane(mediaView);
        mediaPane.setStyle("-fx-background-color: black;");
        
        // Initialize media controller
        mediaController = new MediaController(mediaView, networkManager, true);
        
        // Make the MediaView resize with the window
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        
        setCenter(mediaPane);
        
        // Bottom - Controls
        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10));
        controlsBox.setAlignment(Pos.CENTER);
        
        Button loadButton = new Button("Load Movie");
        loadButton.setOnAction(e -> loadMovie());
        
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> mediaController.play());
        
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> mediaController.pause());
        
        Slider timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setMax(100);
        timeSlider.setPrefWidth(300);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        
        // Bind slider to media position
        mediaController.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            // Only update if not being dragged
            if (!timeSlider.isValueChanging()) {
                double max = mediaController.durationProperty().get();
                timeSlider.setValue(newVal.doubleValue() / max * 100);
            }
        });
        
        // Update media position when slider changes
        timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                double value = timeSlider.getValue() / 100.0;
                double duration = mediaController.durationProperty().get();
                mediaController.seek(Duration.seconds(value * duration));
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
        
        controlsBox.getChildren().addAll(loadButton, playButton, pauseButton, timeSlider, timeLabel);
        setBottom(controlsBox);
        
        // Update connected clients counter
        networkManager.getConnectedClients().addListener((javafx.collections.ListChangeListener.Change<?> c) -> {
            connectedClientsLabel.setText("Connected viewers: " + networkManager.getConnectedClients().size());
        });
    }
    
    private void loadMovie() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Movie File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.wmv")
        );
        
        File file = fileChooser.showOpenDialog(app.getPrimaryStage());
        if (file != null) {
            mediaController.loadMedia(file);
        }
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