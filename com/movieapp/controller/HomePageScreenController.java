package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.movieapp.utils.StageManager;
import java.io.IOException;

public class HomePageScreenController {
    
    @FXML
    private StackPane stackPane;
    
    @FXML
    private Button hostButton;
    
    @FXML
    private Button viewerButton;
    
    @FXML
    private Button learnMoreButton;
    
    @FXML
    private Hyperlink privacyPolicyLink;
    
    @FXML
    private Hyperlink movieNightsLink;
    
    @FXML
    private Hyperlink contactUsLink;
    
    @FXML
    public void initialize() {
        // Initialize button actions
        hostButton.setOnAction(e -> handleHostButton());
        viewerButton.setOnAction(e -> handleViewerButton());
        learnMoreButton.setOnAction(e -> handleLearnMore());
        
        // Initialize hyperlink actions
        privacyPolicyLink.setOnAction(e -> handlePrivacyPolicy());
        movieNightsLink.setOnAction(e -> handleMovieNights());
        contactUsLink.setOnAction(e -> handleContactUs());
    }
    
    private void handleHostButton() {
        try {
            // Load the HostScreen FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/HostScreen.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) stackPane.getScene().getWindow();
            
            // Create new scene and set it
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/host.css").toExternalForm());
            stage.setScene(scene);
            
            // Configure stage using StageManager
            StageManager.getInstance().configureStage(stage);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // You might want to show an error dialog here
        }
    }
    
    private void handleViewerButton() {
        try {
            // Load the ViewerScreen FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/ViewerScreen.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) stackPane.getScene().getWindow();
            
            // Create new scene and set it
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/viewer.css").toExternalForm());
            stage.setScene(scene);
            
            // Configure stage using StageManager
            StageManager.getInstance().configureStage(stage);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // You might want to show an error dialog here
        }
    }
    
    private void handleLearnMore() {
        // TODO: Implement learn more functionality
        // This could open a new window with information about the app
        System.out.println("Learn More clicked");
    }
    
    private void handlePrivacyPolicy() {
        // TODO: Implement privacy policy functionality
        // This could open a new window with the privacy policy
        System.out.println("Privacy Policy clicked");
    }
    
    private void handleMovieNights() {
        // TODO: Implement movie nights statistics functionality
        // This could show statistics about hosted movie nights
        System.out.println("Movie Nights clicked");
    }
    
    private void handleContactUs() {
        // TODO: Implement contact us functionality
        // This could open a contact form or show contact information
        System.out.println("Contact Us clicked");
    }
}
