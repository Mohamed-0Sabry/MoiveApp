package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

public class DemoThemeServerController {
    
    @FXML
    private AnchorPane mainAP;
    
    @FXML
    private ScrollPane messagesPane;
    
    @FXML
    private TextField messageField;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button imageButton;
    
    @FXML
    private Button emojiButton;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private VBox messagesBox;
    
    @FXML
    private HBox windowButtonsContainer;
    
    @FXML
    private Line separatorLine1;
    
    @FXML
    private Line separatorLine2;
    
    @FXML
    public void initialize() {
        // Initialize button actions
        sendButton.setOnAction(e -> handleSendMessage());
        imageButton.setOnAction(e -> handleImageButton());
        emojiButton.setOnAction(e -> handleEmojiButton());
        
        // Initialize window control buttons
        closeButton.setOnAction(e -> System.exit(0));
        
        // Set up message field enter key handler
        messageField.setOnAction(e -> handleSendMessage());
        
        // Initialize scroll pane behavior
        messagesPane.setFitToWidth(true);
        messagesPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }
    
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Add message to the messages box
            // You can implement your message display logic here
            messageField.clear();
        }
    }
    
    private void handleImageButton() {
        // Implement image sending functionality
        System.out.println("Image button clicked");
    }
    
    private void handleEmojiButton() {
        // Implement emoji picker functionality
        System.out.println("Emoji button clicked");
    }
} 