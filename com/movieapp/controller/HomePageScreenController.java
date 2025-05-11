package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.StackPane;
import com.movieapp.utils.StageManager;

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
        learnMoreButton.setOnAction(e -> handleMovieSearch());
        
        // Initialize hyperlink actions
        privacyPolicyLink.setOnAction(e -> handlePrivacyPolicy());
        movieNightsLink.setOnAction(e -> handleMovieNights());
        contactUsLink.setOnAction(e -> handleContactUs());
    }
    
    private void handleHostButton() {
        StageManager.getInstance().loadScene(
            "/com/movieapp/view/HostScreen.fxml",
            "/com/movieapp/styles/host.css",
            "Movie Night - Host"
        );
    }
    
    private void handleViewerButton() {
        StageManager.getInstance().loadScene(
            "/com/movieapp/view/ViewerScreen.fxml",
            "/com/movieapp/styles/viewer.css",
            "Movie Night - Viewer"
        );
    }
    
    private void handleMovieSearch() {
        StageManager.getInstance().loadScene(
            "/com/movieapp/view/MovieSearchScreen.fxml",
            "/com/movieapp/styles/movieSearch.css",
            "Movie Night - Search Movies"
        );
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
