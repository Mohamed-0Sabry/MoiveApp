package com.movieapp.controller.fullscreen;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.StageStyle;
import javafx.scene.Parent;

public class FullscreenController {
    private ImageView imageView;
    private StackPane rootPane;
    private VBox controlsPane;
    private HBox topBar;
    private HBox heartButtonContainer;
    private Pane effectsPane;
    private Stage primaryStage;
    private Stage fullscreenStage;
    private StackPane fullscreenRoot;
    private Parent originalImageViewParent;
    private int originalImageViewIndex;
    private Parent originalHeartButtonParent;
    private int originalHeartButtonIndex;
    private boolean isFullscreen = false;

    public FullscreenController(ImageView imageView, StackPane rootPane, VBox controlsPane, 
                              HBox topBar, HBox heartButtonContainer, Pane effectsPane) {
        this.imageView = imageView;
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
        bindImageViewToFullscreen();
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
        unbindImageViewFromFullscreen();
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
        originalImageViewParent = imageView.getParent();
        originalImageViewIndex = ((Pane) originalImageViewParent).getChildren().indexOf(imageView);
        originalHeartButtonParent = heartButtonContainer.getParent();
        originalHeartButtonIndex = ((Pane) originalHeartButtonParent).getChildren().indexOf(heartButtonContainer);
    }

    private void removeFromOriginalParents() {
        ((Pane) originalImageViewParent).getChildren().remove(imageView);
        ((Pane) originalHeartButtonParent).getChildren().remove(heartButtonContainer);
    }

    private void setupFullscreenOverlay() {
        fullscreenRoot = new StackPane();
        fullscreenRoot.setStyle("-fx-background-color: black;");
        
        // Create a container for the image view to maintain aspect ratio
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: black;");
        imageContainer.getChildren().add(imageView);
        StackPane.setAlignment(imageView, Pos.CENTER);
        
        fullscreenRoot.getChildren().add(imageContainer);
        fullscreenRoot.getChildren().add(heartButtonContainer);
        fullscreenRoot.getChildren().add(effectsPane);
        
        // Configure heart button container
        heartButtonContainer.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        heartButtonContainer.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        StackPane.setAlignment(heartButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(heartButtonContainer, new Insets(0, 10, 10, 0));
        
        // Setup fullscreen stage
        fullscreenStage = new Stage(StageStyle.UNDECORATED);
        fullscreenStage.initOwner(primaryStage);
        fullscreenStage.setFullScreen(true);
        fullscreenStage.setFullScreenExitHint("");
        
        Scene fsScene = new Scene(fullscreenRoot);
        fsScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE: {
                    exitVideoFullscreen();
                    break;
                }
                default:
                    break;
            }
        });
        
        // Add stylesheet
        fsScene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/viewer.css").toExternalForm());
        fullscreenStage.setScene(fsScene);
        
        // Bind image container to scene size
        imageContainer.prefWidthProperty().bind(fsScene.widthProperty());
        imageContainer.prefHeightProperty().bind(fsScene.heightProperty());
    }

    private void bindImageViewToFullscreen() {
        Scene fsScene = fullscreenStage.getScene();
        // Bind to maintain aspect ratio while filling available space
        imageView.fitWidthProperty().bind(fsScene.widthProperty());
        imageView.fitHeightProperty().bind(fsScene.heightProperty());
    }

    private void unbindImageViewFromFullscreen() {
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();
    }

    private void removeFromFullscreenOverlay() {
        if (fullscreenRoot != null) {
            fullscreenRoot.getChildren().remove(imageView);
            fullscreenRoot.getChildren().remove(heartButtonContainer);
            fullscreenRoot.getChildren().remove(effectsPane);
        }
    }

    private void restoreToOriginalParents() {
        ((Pane) originalImageViewParent).getChildren().add(originalImageViewIndex, imageView);
        ((Pane) originalHeartButtonParent).getChildren().add(originalHeartButtonIndex, heartButtonContainer);
        ((StackPane) rootPane).getChildren().add(effectsPane);
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }
} 