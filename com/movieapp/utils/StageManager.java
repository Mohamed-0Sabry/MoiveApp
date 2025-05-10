package com.movieapp.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

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
    
    private void configureStage(Stage stage) {
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // Removes the "Press ESC to exit fullscreen" hint
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
} 