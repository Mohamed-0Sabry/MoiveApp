package com.movieapp.controller;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class HostController {
    @FXML private StackPane showScreen;
    @FXML private Button chatButton;
    
    private AnimationTimer captureTimer;
    private Robot robot;
    private boolean capturing = false;
    private ImageView imageView;

    @FXML
    public void initialize() {
        // Verify FXML injection worked
        if (showScreen == null) {
            System.err.println("Error: showScreen was not injected properly from FXML");
            return;
        }

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
    
    @FXML
    private void openChat() {
        try {
            // Try loading from resources first
            URL fxmlUrl = getClass().getResource("/DemoThemeServer.fxml");
            
            if (fxmlUrl == null) {
                // Fallback to file system path
                File fxmlFile = new File("src/DemoThemeServer.fxml");
                if (fxmlFile.exists()) {
                    fxmlUrl = fxmlFile.toURI().toURL();
                } else {
                    System.err.println("Error: DemoThemeServer.fxml not found in either resources or src/ directory");
                    return;
                }
            }
            
            // Load the chat window
            AnchorPane chatRoot = FXMLLoader.load(fxmlUrl);
            
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat");
            chatStage.setScene(new Scene(chatRoot));
            chatStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load chat window: " + e.getMessage());
            System.err.println("Current working directory: " + System.getProperty("user.dir"));
            
            // Debug: Print classpath
            System.err.println("Classpath: " + System.getProperty("java.class.path"));
        }
    }
    
    public void stopScreenCapture() {
        if (!capturing) return;
        
        capturing = false;
        if (captureTimer != null) {
            captureTimer.stop();
        }
    }
}
