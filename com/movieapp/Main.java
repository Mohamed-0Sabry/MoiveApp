package com.movieapp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("OpenCV library loaded successfully!");
        primaryStage = stage;
        showHostOrViewerChoice();
    }

    private void showHostOrViewerChoice() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/MovieAppScreen.fxml"));
        Scene scene = new Scene(loader.load());

        // Load style
        scene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/movieApp.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Movie Night");
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void switchToViewer() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/movieapp/view/ViewerScreen.fxml"));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(Main.class.getResource("/com/movieapp/styles/viewer.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Movie Night - Viewer");

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        // System.load("C:\\Users\\mahm1\\Downloads\\opencv\\build\\java\\x64\\opencv_java4110.dll"); 

        launch(args);
    }
}