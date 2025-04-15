package com.movienight;

import com.movienight.MovieNightApp;
import com.movienight.NetworkManager;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RoleSelectionScreen extends VBox {
    private final MovieNightApp app;
    private final NetworkManager networkManager;
    private final Button hostButton;
    private final Button viewerButton;
    private final Button testButton;
    private final Label statusLabel;
    private final ProgressIndicator progressIndicator;
    
    public RoleSelectionScreen(MovieNightApp app, NetworkManager networkManager) {
        this.app = app;
        this.networkManager = networkManager;
        
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));
        setSpacing(15);
        
        Label titleLabel = new Label("Movie Night App");
        titleLabel.getStyleClass().add("title-label");
        
        Label instructionLabel = new Label("Choose your role:");
        
        hostButton = new Button("Start as Host");
        hostButton.getStyleClass().add("role-button");
        hostButton.setOnAction(e -> checkAndStartAsHost());
        
        viewerButton = new Button("Join as Viewer");
        viewerButton.getStyleClass().add("role-button");
        viewerButton.setOnAction(e -> startAsViewer());
        
        testButton = new Button("Test Mode (Local)");
        testButton.getStyleClass().add("role-button");
        testButton.setOnAction(e -> app.showTestScreen());
        
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        
        getChildren().addAll(titleLabel, instructionLabel, hostButton, viewerButton, testButton, progressIndicator, statusLabel);
    }
    
    private void checkAndStartAsHost() {
        // Disable buttons during check
        hostButton.setDisable(true);
        viewerButton.setDisable(true);
        testButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Checking if a host already exists...");
        statusLabel.setVisible(true);
        
        // Check if host exists with a small delay to simulate network check
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            boolean hostExists = networkManager.checkIfHostExists();
            
            if (hostExists) {
                statusLabel.setText("A host already exists on the network. Please join as a viewer instead.");
                hostButton.setDisable(true);
                viewerButton.setDisable(false);
                testButton.setDisable(false);
            } else {
                // Start hosting
                networkManager.startHosting();
                app.switchToHostMode();
            }
            
            progressIndicator.setVisible(false);
        });
        pause.play();
    }
    
    private void startAsViewer() {
        // Disable buttons during check
        hostButton.setDisable(true);
        viewerButton.setDisable(true);
        testButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Searching for hosts...");
        statusLabel.setVisible(true);
        
        // Check for hosts with a small delay to simulate network search
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            boolean hostExists = networkManager.checkIfHostExists();
            
            if (hostExists) {
                networkManager.connectToHost();
                app.switchToViewerMode();
            } else {
                statusLabel.setText("No host found. Please try again or become a host.");
                hostButton.setDisable(false);
                viewerButton.setDisable(false);
                testButton.setDisable(false);
            }
            
            progressIndicator.setVisible(false);
        });
        pause.play();
    }
}