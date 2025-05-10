package com.movieapp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.movieapp.network.Server;
import com.movieapp.utils.StageManager;

public class Main extends Application {

    private static final int DEFAULT_PORT = 5555;
    private static Server server;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("OpenCV library loaded successfully!");
        StageManager.getInstance().setPrimaryStage(stage);
        showHostOrViewerChoice();
    }

    private void showHostOrViewerChoice() throws Exception {
        StageManager.getInstance().loadScene(
            "/com/movieapp/view/MovieAppScreen.fxml",
            "/com/movieapp/styles/movieApp.css",
            "Movie Night"
        );
    }

    public static void switchToViewer() throws Exception {
        StageManager.getInstance().loadScene(
            "/com/movieapp/view/ViewerScreen.fxml",
            "/com/movieapp/styles/viewer.css",
            "Movie Night - Viewer"
        );
    }

    public static void switchToMainScreen() throws Exception {
        StageManager.getInstance().loadScene(
            "/com/movieapp/view/MovieAppScreen.fxml",
            "/com/movieapp/styles/movieApp.css",
            "Movie Night"
        );
    }

    public static void switchToHost() throws Exception {
        try {
            // Start the server
            server = new Server();
            server.start(DEFAULT_PORT);
            System.out.println("Server started successfully on port " + DEFAULT_PORT);

            StageManager.getInstance().loadScene(
                "/com/movieapp/view/HostScreen.fxml",
                "/com/movieapp/styles/host.css",
                "Movie Night - Host"
            );

            // Get the controller and set the server
            com.movieapp.controller.HostController controller = 
                (com.movieapp.controller.HostController) StageManager.getInstance()
                    .getPrimaryStage().getScene().getUserData();
            
            if (controller == null) {
                throw new RuntimeException("Failed to get controller from FXML");
            }

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