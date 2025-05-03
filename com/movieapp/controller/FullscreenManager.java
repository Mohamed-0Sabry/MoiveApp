package com.movieapp.controller;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;

public class FullscreenManager {
    private Stage fullscreenStage;
    private StackPane fullscreenRoot;
    private Parent originalMediaViewParent;
    private int originalMediaViewIndex;
    private Parent originalControlsPaneParent;
    private int originalControlsPaneIndex;

    public void enterFullscreen(Node mediaView, Node controlsPane, Stage primaryStage) {
        storeAndMoveToFullscreen(mediaView, true);
        storeAndMoveToFullscreen(controlsPane, false);

        fullscreenRoot = new StackPane();
        fullscreenRoot.setStyle("-fx-background-color: black;");
        fullscreenRoot.getChildren().add(mediaView);
        fullscreenRoot.getChildren().add(controlsPane);
        StackPane.setAlignment(controlsPane, Pos.BOTTOM_CENTER);

        fullscreenStage = new Stage(StageStyle.UNDECORATED);
        fullscreenStage.initModality(Modality.NONE);
        fullscreenStage.setFullScreen(true);
        fullscreenStage.setFullScreenExitHint("");
        Scene fsScene = new Scene(fullscreenRoot, Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
        fsScene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/viewer.css").toExternalForm());
        fullscreenStage.setScene(fsScene);

        fullscreenStage.fullScreenProperty().addListener((obs, wasFull, isNowFull) -> {
            if (!isNowFull) exitFullscreen(mediaView, controlsPane);
        });

        fullscreenStage.show();
    }

    public void exitFullscreen(Node mediaView, Node controlsPane) {
        restoreFromFullscreen(mediaView, originalMediaViewParent, originalMediaViewIndex);
        restoreFromFullscreen(controlsPane, originalControlsPaneParent, originalControlsPaneIndex);
        if (fullscreenStage != null) {
            fullscreenStage.close();
            fullscreenStage = null;
            fullscreenRoot = null;
        }
    }

    private void storeAndMoveToFullscreen(Node node, boolean isMediaView) {
        Parent parent = node.getParent();
        if (parent instanceof Pane) {
            if (isMediaView) {
                originalMediaViewParent = parent;
                originalMediaViewIndex = ((Pane) parent).getChildren().indexOf(node);
            } else {
                originalControlsPaneParent = parent;
                originalControlsPaneIndex = ((Pane) parent).getChildren().indexOf(node);
            }
            ((Pane) parent).getChildren().remove(node);
        }
    }

    private void restoreFromFullscreen(Node node, Parent parent, int index) {
        if (parent != null && node != null && parent instanceof Pane) {
            ((Pane) parent).getChildren().add(index, node);
        }
    }
} 