package com.movienight;

import com.movienight.RoleSelectionScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MovieNightApp extends Application {
    private Stage primaryStage;
    private NetworkManager networkManager;
    private boolean isTestMode = false;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.networkManager = new NetworkManager();
        
        // Check if we're in test mode
        if (getParameters().getRaw().contains("--test")) {
            isTestMode = true;
            showTestScreen();
        } else {
            showRoleSelectionScreen();
        }
        
        primaryStage.setTitle("Movie Night App");
        primaryStage.show();
    }
    
    public void showRoleSelectionScreen() {
        RoleSelectionScreen roleScreen = new RoleSelectionScreen(this, networkManager);
        Scene scene = new Scene(roleScreen, 400, 300);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public void showTestScreen() {
        TestScreen testScreen = new TestScreen(this, networkManager);
        Scene scene = new Scene(testScreen, 500, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public void switchToHostMode() {
        try {
            com.movienight.HostScreen hostScreen = new com.movienight.HostScreen(this, networkManager);
            Scene scene = new Scene(hostScreen, 800, 600);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Movie Night - Host Mode");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void switchToViewerMode() {
        try {
            com.movienight.ViewerScreen viewerScreen = new com.movienight.ViewerScreen(this, networkManager);
            Scene scene = new Scene(viewerScreen, 800, 600);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Movie Night - Viewer Mode");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    @Override
    public void stop() {
        // Clean up resources
        if (networkManager != null) {
            networkManager.shutdown();
        }
    }
}