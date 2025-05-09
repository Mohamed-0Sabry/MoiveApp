package com.movieapp.test;

import com.movieapp.network.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class TestingHost extends Application {
    
    private static final int DEFAULT_PORT = 5555;
    private Server server;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Start the server
            server = new Server();
            server.start(DEFAULT_PORT);
            System.out.println("Server started successfully on port " + DEFAULT_PORT);
            
            // Load the HostScreen FXML
            System.out.println("Loading FXML from: " + getClass().getResource("/com/movieapp/view/HostScreen.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/HostScreen.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            // Get the controller and set the server
            com.movieapp.controller.HostController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("Failed to get controller from FXML");
            }
            System.out.println("Controller initialized and server set");
            
            // Create and show the scene
            Scene scene = new Scene(root);
            String cssPath = getClass().getResource("/com/movieapp/styles/host.css").toExternalForm();
            System.out.println("Loading CSS from: " + cssPath);
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("Movie Night - Host (Test Mode)");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("Stage shown successfully");
            
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Error in start method: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when the application is closed
        if (server != null) {
            server.stop();
            System.out.println("Server stopped");
        }
    }
    
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("Error launching application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
