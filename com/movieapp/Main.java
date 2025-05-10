package com.movieapp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.movieapp.network.Server;
import com.movieapp.utils.StageManager;

public class Main extends Application {

    private static Stage primaryStage;
    private static final int DEFAULT_PORT = 5555;
    private static Server server;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("OpenCV library loaded successfully!");
        primaryStage = stage;
        StageManager.getInstance().configureStage(primaryStage);
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
        StageManager.getInstance().configureStage(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void switchToMainScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/movieapp/view/MovieAppScreen.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Main.class.getResource("/com/movieapp/styles/movieApp.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Movie Night");
        StageManager.getInstance().configureStage(primaryStage);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void switchToHost() throws Exception {
        try {
            // Start the server
            server = new Server();
            server.start(DEFAULT_PORT);
            System.out.println("Server started successfully on port " + DEFAULT_PORT);

            // Load the HostScreen FXML
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/movieapp/view/HostScreen.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(Main.class.getResource("/com/movieapp/styles/host.css").toExternalForm());
            
            // Get the controller and set the server
            com.movieapp.controller.HostController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("Failed to get controller from FXML");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Movie Night - Host");
            StageManager.getInstance().configureStage(primaryStage);
            primaryStage.setOnCloseRequest(event -> {
                if (server != null) {
                    server.stop();
                    System.out.println("Server stopped");
                }
                Platform.exit();
                System.exit(0);
            });
        } catch (Exception e) {
            System.err.println("Error in switchToHost: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public static void main(String[] args) {
        //  System.load("D:\\Study\\ENGINEERING\\25\\Second Term\\Advanced Programming\\Java Projects\\MovieNight\\lib\\opencv_java4110.dll"); 

        launch(args);
    }
}