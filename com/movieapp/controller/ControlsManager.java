package com.movieapp.controller;

import javafx.animation.PauseTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ControlsManager {
    private final VBox controlsPane;
    private final PauseTransition hideControlsTransition;
    private boolean isFullscreen;

    public ControlsManager(VBox controlsPane) {
        this.controlsPane = controlsPane;
        this.hideControlsTransition = new PauseTransition(Duration.seconds(3));
        setupAutoHideControls();
    }

    private void setupAutoHideControls() {
        hideControlsTransition.setOnFinished(event -> {
            if (isFullscreen) controlsPane.setOpacity(0);
        });
    }

    public void handleMouseMovement(MouseEvent event) {
        if (isFullscreen) {
            controlsPane.setOpacity(1);
            hideControlsTransition.stop();
            hideControlsTransition.playFromStart();
        }
    }

    public void setFullscreen(boolean fullscreen) {
        this.isFullscreen = fullscreen;
        if (!fullscreen) {
            hideControlsTransition.stop();
            controlsPane.setOpacity(1);
        }
    }

    public void showControls() {
        controlsPane.setOpacity(1);
        if (isFullscreen) {
            hideControlsTransition.stop();
            hideControlsTransition.playFromStart();
        }
    }
} 