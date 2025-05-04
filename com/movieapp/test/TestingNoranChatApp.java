package com.movieapp.test;

import com.movieapp.controller.DemoThemeServerController;
import com.movieapp.network.Client;
import com.movieapp.model.FileTransfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TestingNoranChatApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/DemoThemeServer.fxml"));
            Parent root = loader.load();
            
            DemoThemeServerController controller = loader.getController();
            Client client = new Client(new Client.MessageListener() {
                @Override
                public void onMessageReceived(String msg) {
                    controller.displayMessage(msg);
                }
                
                @Override
                public void onFileReceived(FileTransfer fileTransfer) {
                }
                
                @Override
                public void onConnectionClosed() {
                }
                
                @Override
                public void onImageReceived(Image image) {
                    controller.onImageReceived(image);
                }
            });
            controller.setClient(client);
            
            client.connectToHost("localhost", 5555);
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("Demo Theme Server Test");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 