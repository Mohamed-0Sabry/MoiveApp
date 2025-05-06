package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;

/**
 * Controller for the Viewer Screen. Handles fullscreen video overlay and heart button overlay.
 */
public class ViewerScreenController {
    // FXML-injected fields
    @FXML private MediaView mediaView;
    @FXML private StackPane rootPane;
    @FXML private VBox controlsPane;
    @FXML private HBox topBar;
    @FXML private HBox heartButtonContainer;

    // State fields
    private boolean isFullscreen = false;
    private Stage primaryStage;

    // For restoring after fullscreen
    private Stage fullscreenStage;
    private StackPane fullscreenRoot;
    private Parent originalMediaViewParent;
    private int originalMediaViewIndex;
    private Parent originalHeartButtonParent;
    private int originalHeartButtonIndex;

    /**
     * Initialize controller and set up primary stage reference.
     */
    @FXML
    private void initialize() {
        rootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                primaryStage = (Stage) newValue.getWindow();
            }
        });
    }

    /**
     * Toggle fullscreen mode when fullscreen button is clicked.
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
     * Enter fullscreen mode: move MediaView and heart button to overlay, hide controls.
     */
    private void enterVideoFullscreen() {
        ensurePrimaryStage();
        isFullscreen = true;
        topBar.setVisible(false);
        storeOriginalParentsAndIndices();
        removeFromOriginalParents();
        setupFullscreenOverlay();
        bindMediaViewToFullscreen();
        controlsPane.setVisible(false);
        heartButtonContainer.setVisible(true);
        fullscreenStage.fullScreenProperty().addListener((obs, wasFull, isNowFull) -> {
            if (!isNowFull && isFullscreen) exitVideoFullscreen();
        });
        fullscreenStage.show();
    }

    /**
     * Exit fullscreen mode: restore MediaView and heart button, show controls.
     */
    private void exitVideoFullscreen() {
        isFullscreen = false;
        topBar.setVisible(true);
        removeFromFullscreenOverlay();
        unbindMediaViewFromFullscreen();
        restoreToOriginalParents();
        controlsPane.setVisible(true);
        heartButtonContainer.setVisible(true);
        if (fullscreenStage != null) {
            fullscreenStage.close();
            fullscreenStage = null;
            fullscreenRoot = null;
        }
    }

    // --- Helper Methods ---

    /** Ensure primaryStage is set. */
    private void ensurePrimaryStage() {
        if (primaryStage == null) {
            primaryStage = (Stage) rootPane.getScene().getWindow();
        }
    }

    /** Store original parents and indices for restoration. */
    private void storeOriginalParentsAndIndices() {
        originalMediaViewParent = mediaView.getParent();
        originalMediaViewIndex = ((Pane) originalMediaViewParent).getChildren().indexOf(mediaView);
        originalHeartButtonParent = heartButtonContainer.getParent();
        originalHeartButtonIndex = ((Pane) originalHeartButtonParent).getChildren().indexOf(heartButtonContainer);
    }

    /** Remove MediaView and heart button from their original parents. */
    private void removeFromOriginalParents() {
        ((Pane) originalMediaViewParent).getChildren().remove(mediaView);
        ((Pane) originalHeartButtonParent).getChildren().remove(heartButtonContainer);
    }

    /** Set up the fullscreen overlay stage and scene. */
    private void setupFullscreenOverlay() {
        fullscreenRoot = new StackPane();
        fullscreenRoot.setStyle("-fx-background-color: black;");
        fullscreenRoot.getChildren().add(mediaView);
        fullscreenRoot.getChildren().add(heartButtonContainer);
        heartButtonContainer.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        heartButtonContainer.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        StackPane.setAlignment(heartButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(heartButtonContainer, new Insets(0, 10, 10, 0));
        fullscreenStage = new Stage(StageStyle.UNDECORATED);
        fullscreenStage.initOwner(primaryStage);
        fullscreenStage.setFullScreen(true);
        fullscreenStage.setFullScreenExitHint("");
        Scene fsScene = new Scene(fullscreenRoot);
        fsScene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/viewer.css").toExternalForm());
        fullscreenStage.setScene(fsScene);
    }

    /** Bind MediaView size to fullscreen scene. */
    private void bindMediaViewToFullscreen() {
        Scene fsScene = fullscreenStage.getScene();
        mediaView.fitWidthProperty().bind(fsScene.widthProperty());
        mediaView.fitHeightProperty().bind(fsScene.heightProperty());
    }

    /** Unbind MediaView size from fullscreen scene. */
    private void unbindMediaViewFromFullscreen() {
        mediaView.fitWidthProperty().unbind();
        mediaView.fitHeightProperty().unbind();
    }

    /** Remove MediaView and heart button from fullscreen overlay. */
    private void removeFromFullscreenOverlay() {
        if (fullscreenRoot != null) {
            fullscreenRoot.getChildren().remove(mediaView);
            fullscreenRoot.getChildren().remove(heartButtonContainer);
        }
    }

    /** Restore MediaView and heart button to their original parents and indices. */
    private void restoreToOriginalParents() {
        ((Pane) originalMediaViewParent).getChildren().add(originalMediaViewIndex, mediaView);
        ((Pane) originalHeartButtonParent).getChildren().add(originalHeartButtonIndex, heartButtonContainer);
    }
} 