package com.movieapp.controller;

import com.movieapp.Main;
import com.movieapp.model.FileTransfer;
import com.movieapp.network.Client;
import com.movieapp.utils.FileUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ViewerController implements Client.MessageListener {
    @FXML
    private TextField hostIpField;
    @FXML
    private TextField portField;
    @FXML
    private ImageView streamImage;
    @FXML
    private Label connectionStatus;

    private Client client;
    private Stage chatStage;
    private ChatController chatController;
    private String lastReceivedFile;
    private byte[] receivedFileData; 


    public void initialize() {
        // Set default port
        portField.setText("5555");
    }

    @FXML
    private void onConnectClicked() {
        if (client != null && client.isConnected) {
            client.stop();
        }

        try {
            String hostIp = hostIpField.getText();
            int port = Integer.parseInt(portField.getText());

            client = new Client(this);
            client.connectToHost(hostIp, port);
            updateConnectionStatus("Connected to " + hostIp + ":" + port);
        } catch (Exception e) {
            updateConnectionStatus("Connection failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDownloadFileClicked() {
        if (lastReceivedFile != null) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Download Location");
            File selectedDirectory = directoryChooser.showDialog(streamImage.getScene().getWindow());

            if (selectedDirectory != null) {
                try {
                    String destinationPath = selectedDirectory.getAbsolutePath() + File.separator + lastReceivedFile;
                    FileTransfer.saveFile(receivedFileData, destinationPath);
                    updateConnectionStatus("File downloaded to: " + destinationPath);
                } catch (Exception e) {
                    updateConnectionStatus("Download failed: " + e.getMessage());
                }
            }
        } else {
            updateConnectionStatus("No file available for download");
        }
    }

    @FXML
    private void onOpenChatClicked() {
        if (chatStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/ChatWindow.fxml"));
                chatStage = new Stage();
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(Main.class.getResource("/com/movieapp/styles/chat.css").toExternalForm());

                chatStage.setScene(scene);
                chatController = loader.getController();
                chatController.setClient(client);
                chatStage.setTitle("Chat");
                chatStage.show();
            } catch (IOException e) {
                updateConnectionStatus("Error opening chat: " + e.getMessage());
            }
        } else {
            chatStage.show();
            chatStage.toFront();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        if (message.startsWith("FRAME:")) {
            String frameData = message.substring(6);
            byte[] imageData = Base64.getDecoder().decode(frameData);
            Image image = new Image(new ByteArrayInputStream(imageData));
            streamImage.setImage(image);
        } else if (message.equals("PLAY")) {
            updateConnectionStatus("Movie Play triggered");
            // TODO: Start media player
        } else if (message.equals("PAUSE")) {
            updateConnectionStatus("Movie Pause triggered");
            // TODO: Pause media player
        } else if (message.equals("STOP")) {
            updateConnectionStatus("Movie Stop triggered");
            // TODO: Stop media player
        }
    }
        
    @Override
    public void onFileReceived(FileTransfer fileTransfer) {
        lastReceivedFile = fileTransfer.getFileName();
        receivedFileData = fileTransfer.getFileData();
        updateConnectionStatus("File received: " + lastReceivedFile);
    }
    
    @Override
    public void onConnectionClosed() {
        updateConnectionStatus("Disconnected from server");
    }

    private void updateConnectionStatus(String message) {
        connectionStatus.setText(message);
    }

    public void stop() {
        if (client != null) {
            client.stop();
        }
        if (chatStage != null) {
            chatStage.close();
        }
    }
}