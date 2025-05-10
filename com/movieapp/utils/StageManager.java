package com.movieapp.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.IOException;
import javafx.geometry.Pos;

public class StageManager {
    private static StageManager instance;
    private Stage primaryStage;
    
    private StageManager() {}
    
    public static StageManager getInstance() {
        if (instance == null) {
            instance = new StageManager();
        }
        return instance;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        configureStage(stage);
    }
    
    public void loadScene(String fxmlPath, String cssPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            if (cssPath != null && !cssPath.isEmpty()) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            
            primaryStage.setScene(scene);
            if (title != null && !title.isEmpty()) {
                primaryStage.setTitle(title);
            }
            
            configureStage(primaryStage);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadNewStage(String fxmlPath, String cssPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            if (cssPath != null && !cssPath.isEmpty()) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            
            Stage newStage = new Stage();
            newStage.setScene(scene);
            if (title != null && !title.isEmpty()) {
                newStage.setTitle(title);
            }
            
            configureStage(newStage);
            newStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading new stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadSlidingPanel(StackPane rootPane, String fxmlPath, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent panel = loader.load();
            
            if (cssPath != null && !cssPath.isEmpty()) {
                panel.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            
            StackPane slidingPanel = new StackPane(panel);
            slidingPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
            slidingPanel.setVisible(false);
            slidingPanel.setTranslateX(400); // Start off-screen
            slidingPanel.setMaxWidth(400); // Set maximum width for the panel
            slidingPanel.setPrefWidth(400);
            
            // Set full height
            slidingPanel.setMaxHeight(Double.MAX_VALUE);
            slidingPanel.setPrefHeight(Double.MAX_VALUE);
            
            // Anchor the panel to the right side
            StackPane.setAlignment(slidingPanel, Pos.CENTER_RIGHT);
            
            rootPane.getChildren().add(slidingPanel);
            
        } catch (IOException e) {
            System.err.println("Error loading sliding panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showSlidingPanel(StackPane panel, boolean show) {
        if (panel == null) return;
        
        panel.setVisible(true);
        Timeline slideAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(panel.translateXProperty(), show ? 400 : 0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(panel.translateXProperty(), show ? 0 : 400)
            )
        );
        
        if (!show) {
            slideAnimation.setOnFinished(e -> panel.setVisible(false));
        }
        
        slideAnimation.play();
    }
    
    private void configureStage(Stage stage) {
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // Removes the "Press ESC to exit fullscreen" hint
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
} 