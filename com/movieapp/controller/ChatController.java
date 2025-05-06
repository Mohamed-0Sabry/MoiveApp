package com.movieapp.controller;

import com.movieapp.network.Client;
import com.movieapp.model.FileTransfer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;


public class ChatController {
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
    @FXML private AnchorPane emojis;
    @FXML private Button emoji1;
    @FXML private Button emoji2;
    @FXML private Button emoji3;
    @FXML private Button emoji4;
    @FXML private Button emoji5;
    @FXML private Button emoji6;

    private Client client;
    
    public void setClient(Client client) {
        this.client = client;
    }

    public void displayMessage(String message) {
        
        if (message.startsWith("CHAT:")) {

            int index = message.indexOf('%', 6);
            String name = message.substring(6, index);
            String content = message.substring(index+1);
            Label msgLabel = new Label(content);
            msgLabel.getStyleClass().add("message-bubble-left");
            Platform.runLater(() -> {
                messagesBox.getChildren().add(new Label(name));
                messagesBox.getChildren().add(msgLabel);
            });

            
        } else if (message.startsWith("INFO_CHAT")) {
            String content = message.substring(10);
                Label msgLabel = new Label(content);
                msgLabel.setAlignment(Pos.CENTER);
                msgLabel.setPrefHeight(18.0);
                msgLabel.setPrefWidth(515.0);
                msgLabel.setTextFill(Color.web("#cccccc"));
                Platform.runLater(() -> {
                    messagesBox.getChildren().add(msgLabel);
            });
        }

    }

    public void onImageReceived(Image image, String name) {
        ImageView imgView = new ImageView(image);
        imgView.setFitWidth(150);
        imgView.setPreserveRatio(true);
        Platform.runLater(() -> {
            messagesBox.getChildren().add(new Label(name));
            messagesBox.getChildren().add(imgView);
        });
    }

    @FXML
    public void initialize() {
        sendButton.setOnAction(e -> handleSendMessage());
        imageButton.setOnAction(e -> handleImageButton());
        emojiButton.setOnAction(e -> handleEmojiButton());
        closeButton.setOnAction(e -> System.exit(0));
        messageField.setOnAction(e -> handleSendMessage());

        emoji1.setOnAction(ev -> messageField.appendText(((Button) ev.getSource()).getText()));
        emoji2.setOnAction(ev -> messageField.appendText(((Button) ev.getSource()).getText()));
        emoji3.setOnAction(ev -> messageField.appendText(((Button) ev.getSource()).getText()));
        emoji4.setOnAction(ev -> messageField.appendText(((Button) ev.getSource()).getText()));
        emoji5.setOnAction(ev -> messageField.appendText(((Button) ev.getSource()).getText()));
        emoji6.setOnAction(ev -> messageField.appendText(((Button) ev.getSource()).getText()));
        

        messagesPane.setFitToWidth(true);
        messagesPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }
    
    private void handleSendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty() && client != null) {
            
            Label msgLabel = new Label(msg);
            msgLabel.getStyleClass().add("message-bubble-right");
            AnchorPane.setRightAnchor(msgLabel, 0.0);
            AnchorPane AP = new AnchorPane();
            
            Platform.runLater(() -> {
                AP.getChildren().add(msgLabel);
                messagesBox.getChildren().add(AP);
            });
            
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
                
                client.sendMessage("IMAGE_CHAT:" + base64);
                
                Image fx = new Image(file.toURI().toString(), 150, 0, true, true);
                ImageView imgView = new ImageView(fx);
                imgView.setFitWidth(150);
                imgView.setPreserveRatio(true);
                AnchorPane.setRightAnchor(imgView, 0.0);
                AnchorPane AP = new AnchorPane();
                
                Platform.runLater(() -> {
                        AP.getChildren().add(imgView);
                        messagesBox.getChildren().add(AP);
                });

            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
            }
        }
    }
    
    private void handleEmojiButton() {
        // String emoji = "\uD83D\uDE03"; 
        // messageField.appendText(emoji);
        emojis.setVisible(!emojis.isVisible());
    }

} 