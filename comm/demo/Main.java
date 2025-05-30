package comm.demo;

import comm.demo.controller.HostController;
import comm.demo.controller.ViewerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    private static HostController hostController;
    private static ViewerController viewerController;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showHostOrViewerChoice();
    }

    private void showHostOrViewerChoice() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/HostScreen.fxml"));
        Scene scene = new Scene(loader.load());

        // Load style
        scene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/host.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Movie Night - Host");
        primaryStage.setOnCloseRequest(event -> {
            if (hostController != null) {
                hostController.stop();
            }
            Platform.exit();
            System.exit(0);
        });
        hostController = loader.getController();
        primaryStage.show();
    }

    public static void switchToViewer() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/movieapp/view/ViewerScreen.fxml"));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(Main.class.getResource("/com/movieapp/styles/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Movie Night - Viewer");
        viewerController = loader.getController();

        primaryStage.setOnCloseRequest(event -> {
            if (viewerController != null) {
                viewerController.stop();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
