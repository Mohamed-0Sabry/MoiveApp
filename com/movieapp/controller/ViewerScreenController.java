package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ViewerScreenController {
    @FXML private MediaView mediaView;
    @FXML private StackPane rootPane;
    @FXML private VBox controlsPane;
    @FXML private HBox topBar;
    @FXML private HBox mediaContainer;

    private boolean isFullscreen = false;
    private final FullscreenManager fullscreenManager;
    private ControlsManager controlsManager;
    private Stage primaryStage;

    public ViewerScreenController() {
        this.fullscreenManager = new FullscreenManager();
    }

    @FXML
    private void initialize() {
        this.controlsManager = new ControlsManager(controlsPane);
        rootPane.setOnMouseMoved(controlsManager::handleMouseMovement);
        primaryStage = (Stage) rootPane.getScene().getWindow();
    }

    @FXML
    private void onFullscreenClicked() {
        if (!isFullscreen) {
            enterVideoFullscreen();
        } else {
            exitVideoFullscreen();
        }
    }

    private void enterVideoFullscreen() {
        isFullscreen = true;
        topBar.setVisible(false);
        fullscreenManager.enterFullscreen(mediaView, controlsPane, primaryStage);
        controlsManager.setFullscreen(true);
        controlsManager.showControls();
    }

    private void exitVideoFullscreen() {
        isFullscreen = false;
        topBar.setVisible(true);
        fullscreenManager.exitFullscreen(mediaView, controlsPane);
        controlsManager.setFullscreen(false);
    }
} 