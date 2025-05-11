package com.movieapp.controller;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import com.movieapp.network.Client;
import com.movieapp.network.Server;
import com.movieapp.model.FileTransfer;
import com.movieapp.utils.StageManager;
import javafx.application.Platform;
import com.movieapp.controller.effects.HeartEffectsController;

public class HostController {
    @FXML private StackPane showScreen;
    @FXML private Button chatButton;
    @FXML private Button fullScreenButton;
    @FXML private ToggleButton audioButton;
    @FXML private ImageView audioOnIcon;
    @FXML private ImageView audioOffIcon;
    @FXML private Button heartButton;
    @FXML private ImageView heartIcon;
    @FXML private Pane effectsPane;
    
    private AnimationTimer captureTimer;
    private Robot robot;
    private boolean capturing = false;
    private ImageView imageView;
    private boolean isFullScreen = false;
    private Double[] originalConstraints = new Double[4];
    private String originalButtonText;
    private Client client;
    private Server server;
    private boolean isAudioStreaming = false;
    private static final int DEFAULT_PORT = 5555;
    private HeartEffectsController heartEffectsController;

    @FXML
    public void initialize() {
        // Verify FXML injection worked
        if (showScreen == null) {
            System.err.println("Error: showScreen was not injected properly from FXML");
            return;
        }

        // Initialize heart effects controller
        heartEffectsController = new HeartEffectsController(heartButton, heartIcon, effectsPane, client);

        try {
            System.setProperty("java.awt.headless", "false");
            robot = new Robot();
            
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            
            // Add safety checks for binding
            if (showScreen.widthProperty() != null && showScreen.heightProperty() != null) {
                imageView.fitWidthProperty().bind(showScreen.widthProperty());
                imageView.fitHeightProperty().bind(showScreen.heightProperty());
            }
            
            showScreen.getChildren().add(imageView);
            startScreenCapture();
            
        } catch (AWTException e) {
            e.printStackTrace();
            if (chatButton != null) {
                chatButton.setDisable(true);
            }
        }

        // Initialize audio controls
        if (audioButton != null) {
            audioButton.setOnAction(event -> toggleAudioStreaming());
            updateAudioButtonState();
        }

        // Start the server
        try {
            server = new Server();
            server.start(DEFAULT_PORT);
            System.out.println("Server started successfully on port " + DEFAULT_PORT);
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize client with audio support
        client = new Client(new Client.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                // Handle chat messages if needed
            }

            @Override
            public void onFileReceived(FileTransfer fileTransfer) {
                // Handle file transfers if needed
            }

            @Override
            public void onConnectionClosed() {
                Platform.runLater(() -> {
                    if (isAudioStreaming) {
                        stopAudioStreaming();
                    }
                });
            }

            @Override
            public void onImageReceived(Image image, String name) {
                // Handle received images if needed
            }

            @Override
            public void onAudioReceived(byte[] audioData, String senderId) {
                // Audio is automatically played by the Client class
                System.out.println("Received audio from: " + senderId);
            }

            @Override
            public void onHeartAnimation(String username, boolean isLiked) {
                Platform.runLater(() -> {
                    // Find the heart controller for this user and show animation
                    if (heartEffectsController != null) {
                        heartEffectsController.showHeartBurst();
                        heartEffectsController.showFloatingHeart();
                    }
                });
            }
        });

        try {
            client.connectToHost("localhost", DEFAULT_PORT);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }

        // Set up ESC key handler for full screen
        showScreen.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE && isFullScreen) {
                fullScreenClicked();
            }
        });
        
        originalButtonText = fullScreenButton.getText();
    }

    @FXML
    private void openChat() {
        try {
            StageManager.getInstance().loadNewStage(
                "/com/movieapp/view/DemoThemeServer.fxml",
                "/com/movieapp/styles/demoTheme.css",
                "Chat"
            );
        } catch (Exception e) {
            System.err.println("Failed to load chat window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void startScreenCapture() {
        if (capturing || robot == null) return;
        
        capturing = true;
        captureTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                captureAndDisplayScreen();
            }
        };
        captureTimer.start();
    }
    
    private void captureAndDisplayScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage awtImage = robot.createScreenCapture(screenRect);
            
            WritableImage fxImage = new WritableImage(awtImage.getWidth(), awtImage.getHeight());
            java.awt.image.PixelGrabber pg = new java.awt.image.PixelGrabber(
                awtImage, 0, 0, awtImage.getWidth(), awtImage.getHeight(),
                ((java.awt.image.DataBufferInt) awtImage.getRaster().getDataBuffer()).getData(), 0, awtImage.getWidth()
            );
            
            pg.grabPixels();
            
            fxImage.getPixelWriter().setPixels(0, 0, awtImage.getWidth(), awtImage.getHeight(),
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                ((java.awt.image.DataBufferInt) awtImage.getRaster().getDataBuffer()).getData(),
                0, awtImage.getWidth()
            );
            
            javafx.application.Platform.runLater(() -> {
                if (imageView != null) {
                    imageView.setImage(fxImage);
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stopScreenCapture() {
        if (!capturing) return;
        
        capturing = false;
        if (captureTimer != null) {
            captureTimer.stop();
        }
    }

    @FXML 
    private void fullScreenClicked() {
        isFullScreen = !isFullScreen;
        
        if (isFullScreen) {
            // Store original constraints
            originalConstraints[0] = AnchorPane.getTopAnchor(showScreen);
            originalConstraints[1] = AnchorPane.getBottomAnchor(showScreen);
            originalConstraints[2] = AnchorPane.getLeftAnchor(showScreen);
            originalConstraints[3] = AnchorPane.getRightAnchor(showScreen);
            
            // Set fullscreen constraints
            AnchorPane.clearConstraints(showScreen);
            AnchorPane.setTopAnchor(showScreen, 0.0);
            AnchorPane.setBottomAnchor(showScreen, 0.0);
            AnchorPane.setLeftAnchor(showScreen, 0.0);
            AnchorPane.setRightAnchor(showScreen, 0.0);
            
            // Hide all buttons
            setButtonsVisible(false);
            
            // Request focus for key events
            showScreen.requestFocus();
        } else {
            // Restore original constraints
            AnchorPane.setTopAnchor(showScreen, originalConstraints[0]);
            AnchorPane.setBottomAnchor(showScreen, originalConstraints[1]);
            AnchorPane.setLeftAnchor(showScreen, originalConstraints[2]);
            AnchorPane.setRightAnchor(showScreen, originalConstraints[3]);
            
            // Show all buttons
            setButtonsVisible(true);
        }
        
        // Update button text
        fullScreenButton.setText(isFullScreen ? "Exit Full Screen" : "Full Screen");
    }
    
    private void setButtonsVisible(boolean visible) {
        chatButton.setVisible(visible);
        fullScreenButton.setVisible(visible);
        audioButton.setVisible(visible);
    }

    private void toggleAudioStreaming() {
        if (client != null) {
            if (!isAudioStreaming) {
                startAudioStreaming();
            } else {
                stopAudioStreaming();
            }
        }
    }

    private void startAudioStreaming() {
        if (client != null) {
            client.startAudioStreaming();
            isAudioStreaming = true;
            updateAudioButtonState();
        }
    }

    private void stopAudioStreaming() {
        if (client != null) {
            client.stopAudioStreaming();
            isAudioStreaming = false;
            updateAudioButtonState();
        }
    }

    private void updateAudioButtonState() {
        if (audioButton != null && audioOnIcon != null && audioOffIcon != null) {
            audioOnIcon.setVisible(isAudioStreaming);
            audioOffIcon.setVisible(!isAudioStreaming);
        }
    }

    public void stop() {
        stopScreenCapture();
        stopAudioStreaming();
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }
    }
}
