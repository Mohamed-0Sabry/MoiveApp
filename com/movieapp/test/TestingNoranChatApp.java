package com.movieapp.test;

import com.movieapp.controller.ChatController;
import com.movieapp.network.Client;
import com.movieapp.model.FileTransfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.Window;
import javafx.stage.StageStyle;

public class TestingNoranChatApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/movieapp/view/ChatView.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Noran Chat");
            primaryStage.getIcons().add(new Image("/com/movieapp/icon/icon.png"));
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                closeApp();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeApp() {
        // Add any additional logic you want to execute before closing the application
        System.out.println("Application is closing...");
        // ...
    }

    public static void main(String[] args) {
        launch(args);
    }
}