package com.movienight;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TestScreen extends VBox {
    private final MovieNightApp app;
    private final NetworkManager networkManager;
    
    public TestScreen(MovieNightApp app, NetworkManager networkManager) {
        this.app = app;
        this.networkManager = networkManager;
        
        setSpacing(20);
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        
        // Title
        Label titleLabel = new Label("Movie Night - Test Mode");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Description
        Label descriptionLabel = new Label("This is a test mode that allows you to run both host and viewer on the same machine.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);
        
        // Test buttons
        Button testHostButton = new Button("Test Host Mode");
        testHostButton.setPrefWidth(200);
        testHostButton.setOnAction(e -> openHostScreen());
        
        Button testViewerButton = new Button("Test Viewer Mode");
        testViewerButton.setPrefWidth(200);
        testViewerButton.setOnAction(e -> openViewerScreen());
        
        Button testBothButton = new Button("Test Both Modes Simultaneously");
        testBothButton.setPrefWidth(200);
        testBothButton.setOnAction(e -> openBothScreens());
        
        Button backButton = new Button("Back to Role Selection");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> app.showRoleSelectionScreen());
        
        // Add all elements to the layout
        getChildren().addAll(
            titleLabel,
            descriptionLabel,
            testHostButton,
            testViewerButton,
            testBothButton,
            backButton
        );
    }
    
    private void openHostScreen() {
        // Set the network manager to host mode
        NetworkManager.enableTestMode(8888, 8889);  // Use consistent ports for host
        NetworkManager.setTestRole(true);
        networkManager.shutdown();  // Reset any existing connections
        
        // Create a new stage for the host screen
        Stage hostStage = new Stage();
        hostStage.setTitle("Movie Night - Host Mode (Test)");
        
        // Create a new network manager for the host
        NetworkManager hostNetworkManager = new NetworkManager();
        hostNetworkManager.startHosting();
        
        // Create the host screen
        HostScreen hostScreen = new HostScreen(app, hostNetworkManager);
        Scene scene = new Scene(hostScreen, 800, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        // Set the scene and show the stage
        hostStage.setScene(scene);
        hostStage.show();
        
        // Clean up when the window is closed
        hostStage.setOnCloseRequest(e -> hostNetworkManager.shutdown());
    }
    
    private void openViewerScreen() {
        // Set the network manager to viewer mode
        NetworkManager.enableTestMode(8890, 8891);  // Use different ports for viewer
        NetworkManager.setTestRole(false);
        networkManager.shutdown();  // Reset any existing connections
        
        // Create a new stage for the viewer screen
        Stage viewerStage = new Stage();
        viewerStage.setTitle("Movie Night - Viewer Mode (Test)");
        
        // Create a new network manager for the viewer
        NetworkManager viewerNetworkManager = new NetworkManager();
        
        // Create the viewer screen
        ViewerScreen viewerScreen = new ViewerScreen(app, viewerNetworkManager);
        Scene scene = new Scene(viewerScreen, 800, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        // Connect to local host (after slight delay to ensure host is ready)
        new Thread(() -> {
            try {
                Thread.sleep(500);
                viewerNetworkManager.checkIfHostExists();
                Thread.sleep(500);
                if (viewerNetworkManager.hostExistsProperty().get()) {
                    viewerNetworkManager.connectToHost();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
        
        // Set the scene and show the stage
        viewerStage.setScene(scene);
        viewerStage.show();
        
        // Clean up when the window is closed
        viewerStage.setOnCloseRequest(e -> viewerNetworkManager.shutdown());
    }
    
    private void openBothScreens() {
        // First start the host, then the viewer with a slight delay
        openHostScreen();
        
        // Open viewer with a slight delay to ensure host is ready
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                javafx.application.Platform.runLater(this::openViewerScreen);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}