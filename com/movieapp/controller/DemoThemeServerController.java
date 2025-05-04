package com.movieapp.controller;

import com.movieapp.network.Client;
import com.movieapp.model.FileTransfer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;


public class DemoThemeServerController {
    @FXML private AnchorPane mainAP;
    @FXML private ScrollPane messagesPane;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button imageButton;
    @FXML private Button emojiButton;
    @FXML private Button closeButton;
    @FXML private VBox messagesBox;
    @FXML private HBox windowButtonsContainer;
    @FXML private Line separatorLine1;
    @FXML private Line separatorLine2;

    private Client client;
    
    public void setClient(Client client) {
        this.client = client;
    }

    public void displayMessage(String message) {
        
        if (message.startsWith("CHAT:")) {
            String content = message.substring(5);
            Platform.runLater(() -> messagesBox.getChildren().add(new Label(content)));
        }
    }

    public void onImageReceived(Image image) {
        ImageView imgView = new ImageView(image);
        imgView.setFitWidth(150);
        imgView.setPreserveRatio(true);
        Platform.runLater(() -> messagesBox.getChildren().add(imgView));
    }

    @FXML
    public void initialize() {
        sendButton.setOnAction(e -> handleSendMessage());
        imageButton.setOnAction(e -> handleImageButton());
        emojiButton.setOnAction(e -> handleEmojiButton());
        closeButton.setOnAction(e -> System.exit(0));
        messageField.setOnAction(e -> handleSendMessage());

        messagesPane.setFitToWidth(true);
        messagesPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }
    
    private void handleSendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty() && client != null) {
            
            Platform.runLater(() -> messagesBox.getChildren().add(new Label("Me: " + msg)));
            
            client.sendMessage("CHAT:" + msg);
            messageField.clear();
        }
    }

  private void handleImageButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image to Send");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(mainAP.getScene().getWindow());

        if (file != null && client != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                
                client.sendMessage("IMAGE:" + base64);
                
                Image fx = new Image(file.toURI().toString(), 150, 0, true, true);
                onImageReceived(fx);
            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
            }
        }
    }
    
    private void handleEmojiButton() {
        String emoji = "\uD83D\uDE03"; 
        messageField.appendText(emoji);
    }
} 