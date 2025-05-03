package com.movieapp.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * Controller for the Viewer Screen. Handles fullscreen video overlay and auto-hiding controls.
 */
public class ViewerScreenController {
    // FXML-injected fields
    @FXML private MediaView mediaView;
    @FXML private StackPane rootPane;
    @FXML private VBox controlsPane;
    @FXML private HBox topBar;
    @FXML private HBox mediaContainer;
    @FXML private HBox heartButtonContainer;

    // Fullscreen state
    private boolean isFullscreen = false;
    private Stage fullscreenStage;
    private StackPane fullscreenRoot;

    // For restoring original layout
    private Parent originalMediaViewParent;
    private int originalMediaViewIndex;
    private Parent originalControlsPaneParent;
    private int originalControlsPaneIndex;
    private Parent originalHeartButtonParent;
    private int originalHeartButtonIndex;

    // Controls auto-hide
    private PauseTransition hideControlsTransition;

    @FXML
    private void initialize() {
        setupAutoHideControls();
        rootPane.setOnMouseMoved(this::handleMouseMovement);
    }

    /**
     * Set up the auto-hide transition for the controls overlay.
     */
    private void setupAutoHideControls() {
        hideControlsTransition = new PauseTransition(Duration.seconds(3));
        hideControlsTransition.setOnFinished(event -> {
            if (isFullscreen) heartButtonContainer.setOpacity(0);
        });
    }

    /**
     * Show controls overlay and restart auto-hide timer.
     */
    private void handleMouseMovement(MouseEvent event) {
        if (isFullscreen) {
            heartButtonContainer.setOpacity(1);
            hideControlsTransition.stop();
            hideControlsTransition.playFromStart();
        }
    }

    /**
     * Toggle video-only fullscreen overlay.
     */
    @FXML
    private void onFullscreenClicked() {
        if (!isFullscreen) {
            enterVideoFullscreen();
        } else {
            exitVideoFullscreen();
        }
    }

    /**
     * Enter video-only fullscreen overlay mode.
     */
    private void enterVideoFullscreen() {
        isFullscreen = true;
        topBar.setVisible(false);
        storeAndMoveToFullscreen(mediaView);
        storeAndMoveToFullscreen(heartButtonContainer);

        fullscreenRoot = new StackPane();
        fullscreenRoot.setStyle("-fx-background-color: black;");
        fullscreenRoot.getChildren().add(mediaView);
        fullscreenRoot.getChildren().add(heartButtonContainer);
        StackPane.setAlignment(heartButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(heartButtonContainer, new Insets(0, 40, 40, 0)); // 40px from right and bottom

        fullscreenStage = new Stage(StageStyle.UNDECORATED);
        fullscreenStage.initModality(Modality.NONE);
        fullscreenStage.setFullScreen(true);
        fullscreenStage.setFullScreenExitHint("");
        Scene fsScene = new Scene(fullscreenRoot, Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
        fsScene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/viewer.css").toExternalForm());
        fullscreenStage.setScene(fsScene);

        heartButtonContainer.setOpacity(1);
        hideControlsTransition.playFromStart();
        fullscreenRoot.setOnMouseMoved(this::handleMouseMovement);

        fullscreenStage.fullScreenProperty().addListener((obs, wasFull, isNowFull) -> {
            if (!isNowFull && isFullscreen) exitVideoFullscreen();
        });

        fullscreenStage.show();
    }

    /**
     * Exit video-only fullscreen overlay mode and restore layout.
     */
    private void exitVideoFullscreen() {
        isFullscreen = false;
        hideControlsTransition.stop();
        topBar.setVisible(true);
        restoreFromFullscreen(mediaView, originalMediaViewParent, originalMediaViewIndex);
        restoreFromFullscreen(heartButtonContainer, originalHeartButtonParent, originalHeartButtonIndex);
        heartButtonContainer.setOpacity(1);
        if (fullscreenStage != null) {
            fullscreenStage.close();
            fullscreenStage = null;
            fullscreenRoot = null;
        }
    }

    /**
     * Store the original parent/index and remove the node from its parent.
     */
    private void storeAndMoveToFullscreen(Node node) {
        Parent parent = node.getParent();
        if (node == mediaView) {
            originalMediaViewParent = parent;
            originalMediaViewIndex = ((Pane) parent).getChildren().indexOf(mediaView);
            ((Pane) parent).getChildren().remove(mediaView);
        } else if (node == heartButtonContainer) {
            originalHeartButtonParent = parent;
            originalHeartButtonIndex = ((Pane) parent).getChildren().indexOf(heartButtonContainer);
            ((Pane) parent).getChildren().remove(heartButtonContainer);
        }
    }

    /**
     * Restore the node to its original parent and index.
     */
    private void restoreFromFullscreen(Node node, Parent parent, int index) {
        if (parent != null && node != null) {
            ((Pane) parent).getChildren().add(index, node);
        }
    }
} 