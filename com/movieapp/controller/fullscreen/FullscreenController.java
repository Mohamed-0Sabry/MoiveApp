package com.movieapp.controller.fullscreen;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.StageStyle;
import javafx.scene.Parent;

public class FullscreenController {
    private MediaView mediaView;
    private StackPane rootPane;
    private VBox controlsPane;
    private HBox topBar;
    private HBox heartButtonContainer;
    private Pane effectsPane;
    private Stage primaryStage;
    private Stage fullscreenStage;
    private StackPane fullscreenRoot;
    private Parent originalMediaViewParent;
    private int originalMediaViewIndex;
    private Parent originalHeartButtonParent;
    private int originalHeartButtonIndex;
    private boolean isFullscreen = false;

    public FullscreenController(MediaView mediaView, StackPane rootPane, VBox controlsPane, 
                              HBox topBar, HBox heartButtonContainer, Pane effectsPane) {
        this.mediaView = mediaView;
        this.rootPane = rootPane;
        this.controlsPane = controlsPane;
        this.topBar = topBar;
        this.heartButtonContainer = heartButtonContainer;
        this.effectsPane = effectsPane;
    }

    public void toggleFullscreen() {
        if (!isFullscreen) {
            enterVideoFullscreen();
        } else {
            exitVideoFullscreen();
        }
    }

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

    private void ensurePrimaryStage() {
        if (primaryStage == null) {
            primaryStage = (Stage) rootPane.getScene().getWindow();
        }
    }

    private void storeOriginalParentsAndIndices() {
        originalMediaViewParent = mediaView.getParent();
        originalMediaViewIndex = ((Pane) originalMediaViewParent).getChildren().indexOf(mediaView);
        originalHeartButtonParent = heartButtonContainer.getParent();
        originalHeartButtonIndex = ((Pane) originalHeartButtonParent).getChildren().indexOf(heartButtonContainer);
    }

    private void removeFromOriginalParents() {
        ((Pane) originalMediaViewParent).getChildren().remove(mediaView);
        ((Pane) originalHeartButtonParent).getChildren().remove(heartButtonContainer);
    }

    private void setupFullscreenOverlay() {
        fullscreenRoot = new StackPane();
        fullscreenRoot.setStyle("-fx-background-color: black;");
        fullscreenRoot.getChildren().add(mediaView);
        fullscreenRoot.getChildren().add(heartButtonContainer);
        fullscreenRoot.getChildren().add(effectsPane);
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

    private void bindMediaViewToFullscreen() {
        Scene fsScene = fullscreenStage.getScene();
        mediaView.fitWidthProperty().bind(fsScene.widthProperty());
        mediaView.fitHeightProperty().bind(fsScene.heightProperty());
    }

    private void unbindMediaViewFromFullscreen() {
        mediaView.fitWidthProperty().unbind();
        mediaView.fitHeightProperty().unbind();
    }

    private void removeFromFullscreenOverlay() {
        if (fullscreenRoot != null) {
            fullscreenRoot.getChildren().remove(mediaView);
            fullscreenRoot.getChildren().remove(heartButtonContainer);
            fullscreenRoot.getChildren().remove(effectsPane);
        }
    }

    private void restoreToOriginalParents() {
        ((Pane) originalMediaViewParent).getChildren().add(originalMediaViewIndex, mediaView);
        ((Pane) originalHeartButtonParent).getChildren().add(originalHeartButtonIndex, heartButtonContainer);
        ((StackPane) rootPane).getChildren().add(effectsPane);
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }
} 