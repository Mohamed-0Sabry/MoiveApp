package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import com.movieapp.network.Client;
import com.movieapp.model.FileTransfer;
import javafx.scene.image.Image;
import java.io.IOException;

import com.movieapp.utils.AudioStreamUtils;
import com.movieapp.utils.StageManager;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import javax.sound.sampled.Mixer;
import javafx.scene.control.ToggleButton;

/**
 * Controller for the Viewer Screen. Handles fullscreen video overlay and heart button overlay.
 */
public class ViewerScreenController {
    // FXML-injected fields
    @FXML private MediaView mediaView;
    @FXML private StackPane rootPane;
    @FXML private VBox controlsPane;
    @FXML private HBox topBar;
    @FXML private HBox heartButtonContainer;
    @FXML private Button heartButton;
    @FXML private ImageView heartIcon;
    @FXML private Button muteButton;
    @FXML private ImageView volumeOnIcon;
    @FXML private ImageView volumeMuteIcon;
    @FXML private Slider volumeSlider;
    @FXML private Pane effectsPane;
    @FXML public StackPane chatPanel;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private VBox messagesBox;
    @FXML private ScrollPane messagesPane;
    @FXML private ToggleButton audioButton;
    @FXML private ImageView audioOnIcon;
    @FXML private ImageView audioOffIcon;
    @FXML private ProgressBar audioLevelMeter;
    @FXML private ComboBox<String> micSelector;

    // State fields
    private boolean isFullscreen = false;
    private Stage primaryStage;

    // For restoring after fullscreen
    private Stage fullscreenStage;
    private StackPane fullscreenRoot;
    private Parent originalMediaViewParent;
    private int originalMediaViewIndex;
    private Parent originalHeartButtonParent;
    private int originalHeartButtonIndex;

    private boolean isLiked = false;
    private boolean isMuted = false;
    private double previousVolume = 1.0;

    private Client client;
    private ChatController chatController;
    private boolean isChatOpen = false;

    private boolean isAudioStreaming = false;
    private Mixer.Info selectedMicrophone;
    private AnimationTimer audioLevelTimer;

    /**
     * Initialize controller and set up primary stage reference.
     */
    @FXML
    private void initialize() {
        rootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                primaryStage = (Stage) newValue.getWindow();
            }
        });
        heartButton.setOnAction(event -> {
            animateHeart();
            showHeartBurst();
        });
        
        // Initialize volume controls
        setupVolumeControls();
        // Make effectsPane mouse transparent so it doesn't block button clicks
        if (effectsPane != null) {
            effectsPane.setMouseTransparent(true);
        }

        // Initialize audio level meter if available
        if (audioLevelMeter != null) {
            audioLevelMeter.setProgress(0);
            audioLevelMeter.setStyle("-fx-accent: green;");
            startAudioLevelMeter();
        }
        
        // Initialize microphone selector if available
        if (micSelector != null) {
            updateAvailableMicrophones();
            micSelector.setOnAction(e -> setSelectedMicrophone());
        }

        // Initialize chat panel
        try {
            // Initialize client with proper message listener first
            client = new Client(new Client.MessageListener() {
                @Override
                public void onMessageReceived(String msg) {
                    if (chatController != null) {
                        chatController.displayMessage(msg);
                    }
                }
                
                @Override
                public void onFileReceived(FileTransfer fileTransfer) {
                    // Handle file transfer if needed
                }
                
                @Override
                public void onConnectionClosed() {
                    Platform.runLater(() -> {
                        // Handle connection closed
                        System.out.println("Connection to server closed");
                    });
                }
                
                @Override
                public void onImageReceived(Image image, String name) {
                    if (chatController != null) {
                        chatController.onImageReceived(image, name);
                    }
                }

                @Override
                public void onAudioReceived(byte[] audioData, String senderId) {
                    // Play all received audio - the server handles routing
                    AudioStreamUtils.playAudioStream(audioData);
                    
                    // Update volume meter based on the audio data
                    if (audioLevelMeter != null) {
                        updateVolumeMeter(audioData);
                    }
                }
            });

            // Connect to host before loading the chat panel
            try {
                client.connectToHost("localhost", 5555);
                System.out.println("Successfully connected to chat server");
            } catch (Exception e) {
                System.err.println("Error connecting to chat server: " + e.getMessage());
                return; // Exit if connection fails
            }

            // Now load the chat panel
            StageManager.getInstance().loadSlidingPanel(rootPane, "/com/movieapp/view/DemoThemeServer.fxml", "/com/movieapp/styles/css-stylesheet.css");
            chatPanel = (StackPane) rootPane.getChildren().get(rootPane.getChildren().size() - 1);
            
            // Get the controller from the panel's user data
            chatController = (ChatController) chatPanel.getUserData();
            if (chatController != null) {
                System.out.println("Setting client in chat controller...");
                chatController.setClient(client);
            } else {
                System.err.println("Failed to get chat controller!");
            }

            // Make sure the chat panel stays on top
            chatPanel.toFront();
        } catch (Exception e) {
            System.err.println("Error initializing chat panel: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize audio controls
        if (audioButton != null) {
            audioButton.setOnAction(event -> toggleAudioStreaming());
            updateAudioButtonState();
        }
    }

    /**
     * Toggle fullscreen mode when fullscreen button is clicked.
     */
    @FXML
    private void onFullscreenClicked() {
        if (!isFullscreen) {
            enterVideoFullscreen();
        } else {
            exitVideoFullscreen();
        }
    }

    /**
     * Enter fullscreen mode: move MediaView and heart button to overlay, hide controls.
     */
    private void enterVideoFullscreen() {
        ensurePrimaryStage();
        isFullscreen = true;
        topBar.setVisible(false);
        storeOriginalParentsAndIndices();
        removeFromOriginalParents();
        setupFullscreenOverlay();
        bindMediaViewToFullscreen();
        controlsPane.setVisible(false);
        heartButtonContainer.setVisible(true);
        fullscreenStage.fullScreenProperty().addListener((obs, wasFull, isNowFull) -> {
            if (!isNowFull && isFullscreen) exitVideoFullscreen();
        });
        fullscreenStage.show();
    }

    /**
     * Exit fullscreen mode: restore MediaView and heart button, show controls.
     */
    private void exitVideoFullscreen() {
        isFullscreen = false;
        topBar.setVisible(true);
        removeFromFullscreenOverlay();
        unbindMediaViewFromFullscreen();
        restoreToOriginalParents();
        controlsPane.setVisible(true);
        heartButtonContainer.setVisible(true);
        if (fullscreenStage != null) {
            fullscreenStage.close();
            fullscreenStage = null;
            fullscreenRoot = null;
        }
    }

    /**
     *function to return to the MovieAppScreen when the back button is clicked.
     */
    @FXML
    private void onBackButtonClicked() {
        try {
            com.movieapp.Main.switchToMainScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onChatButtonClicked() {
        isChatOpen = !isChatOpen;
        chatPanel.setVisible(true);
        StageManager.getInstance().showSlidingPanel(chatPanel, isChatOpen);
        if (isChatOpen) {
            chatPanel.toFront();
            // Ensure chat panel is properly initialized
            Platform.runLater(() -> {
                if (chatController != null) {
                    chatController.focusMessageField();
                }
            });
        }    
    }

    public boolean getChatState(){
        return this.isChatOpen;
    }
    

    /** Ensure primaryStage is set. */
    private void ensurePrimaryStage() {
        if (primaryStage == null) {
            primaryStage = (Stage) rootPane.getScene().getWindow();
        }
    }

    /** Store original parents and indices for restoration. */
    private void storeOriginalParentsAndIndices() {
        originalMediaViewParent = mediaView.getParent();
        originalMediaViewIndex = ((Pane) originalMediaViewParent).getChildren().indexOf(mediaView);
        originalHeartButtonParent = heartButtonContainer.getParent();
        originalHeartButtonIndex = ((Pane) originalHeartButtonParent).getChildren().indexOf(heartButtonContainer);
    }

    /** Remove MediaView and heart button from their original parents. */
    private void removeFromOriginalParents() {
        ((Pane) originalMediaViewParent).getChildren().remove(mediaView);
        ((Pane) originalHeartButtonParent).getChildren().remove(heartButtonContainer);
    }

    /** Set up the fullscreen overlay stage and scene. */
    private void setupFullscreenOverlay() {
        fullscreenRoot = new StackPane();
        fullscreenRoot.setStyle("-fx-background-color: black;");
        fullscreenRoot.getChildren().add(mediaView);
        fullscreenRoot.getChildren().add(heartButtonContainer);
        fullscreenRoot.getChildren().add(effectsPane);
        heartButtonContainer.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        heartButtonContainer.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        StackPane.setAlignment(heartButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(heartButtonContainer, new Insets(0, 10, 10, 0));
        fullscreenStage = new Stage(StageStyle.UNDECORATED);
        fullscreenStage.initOwner(primaryStage);
        fullscreenStage.setFullScreen(true);
        fullscreenStage.setFullScreenExitHint("");
        Scene fsScene = new Scene(fullscreenRoot);
        fsScene.getStylesheets().add(getClass().getResource("/com/movieapp/styles/viewer.css").toExternalForm());
        fullscreenStage.setScene(fsScene);
    }

    /** Bind MediaView size to fullscreen scene. */
    private void bindMediaViewToFullscreen() {
        Scene fsScene = fullscreenStage.getScene();
        mediaView.fitWidthProperty().bind(fsScene.widthProperty());
        mediaView.fitHeightProperty().bind(fsScene.heightProperty());
    }

    /** Unbind MediaView size from fullscreen scene. */
    private void unbindMediaViewFromFullscreen() {
        mediaView.fitWidthProperty().unbind();
        mediaView.fitHeightProperty().unbind();
    }

    /** Remove MediaView and heart button from fullscreen overlay. */
    private void removeFromFullscreenOverlay() {
        if (fullscreenRoot != null) {
            fullscreenRoot.getChildren().remove(mediaView);
            fullscreenRoot.getChildren().remove(heartButtonContainer);
            fullscreenRoot.getChildren().remove(effectsPane);
        }
    }

    /** Restore MediaView and heart button to their original parents and indices. */
    private void restoreToOriginalParents() {
        ((Pane) originalMediaViewParent).getChildren().add(originalMediaViewIndex, mediaView);
        ((Pane) originalHeartButtonParent).getChildren().add(originalHeartButtonIndex, heartButtonContainer);
        ((StackPane) rootPane).getChildren().add(effectsPane);
    }

    private void animateHeart() {
        // Create a combined scale, rotation, and glow animation
        Timeline animationTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(heartIcon.scaleXProperty(), 1),
                new KeyValue(heartIcon.scaleYProperty(), 1),
                new KeyValue(heartIcon.rotateProperty(), 0),
                new KeyValue(heartIcon.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.millis(100),
                new KeyValue(heartIcon.scaleXProperty(), 1.4),
                new KeyValue(heartIcon.scaleYProperty(), 1.4),
                new KeyValue(heartIcon.rotateProperty(), 30),
                new KeyValue(heartIcon.opacityProperty(), 1.2)
            ),
            new KeyFrame(Duration.millis(200),
                new KeyValue(heartIcon.scaleXProperty(), 1),
                new KeyValue(heartIcon.scaleYProperty(), 1),
                new KeyValue(heartIcon.rotateProperty(), 0),
                new KeyValue(heartIcon.opacityProperty(), 1)
            )
        );
        
        // Toggle liked state
        isLiked = !isLiked;
        
        // Update styles based on liked state
        if (isLiked) {
            heartButton.setStyle("-fx-background-color: transparent; " +
                               "-fx-border-radius: 50%; " +
                               "-fx-border: 2px solid #ff4d4d; " +
                               "-fx-cursor: hand; " +
                               "-fx-font-weight: bold; " +
                               "-fx-effect: dropshadow(gaussian, #ff9999, 10, 0.3, 0, 0);");
            heartIcon.setStyle("-fx-effect: dropshadow(gaussian, #ff9999, 12, 0.4, 0, 0);");
        } else {
            heartButton.setStyle("-fx-background-color: transparent; " +
                               "-fx-border-radius: 50%; " +
                               "-fx-border: 2px solid #cccccc; " +
                               "-fx-cursor: hand; " +
                               "-fx-font-weight: bold; " +
                               "-fx-effect: none;");
            heartIcon.setStyle("-fx-effect: none;");
        }
        
        // Play the animation
        animationTimeline.play();
    }

    private void setupVolumeControls() {
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaView.getMediaPlayer() != null) {
                double volume = newVal.doubleValue() / 100.0;
                mediaView.getMediaPlayer().setVolume(volume);
            }
            // Icon switching logic (always runs)
            if (volumeOnIcon != null && volumeMuteIcon != null) {
                boolean isMuted = newVal.doubleValue() == 0;
                volumeOnIcon.setVisible(!isMuted);
                volumeMuteIcon.setVisible(isMuted);
            }
        });

        muteButton.setOnAction(event -> toggleMute());
    }

    private void toggleMute() {
        if (volumeSlider.getValue() == 0) {
            volumeSlider.setValue(100);
        } else {
            volumeSlider.setValue(0);
        }
    }

    private void showHeartBurst() {
        for (int i = 0; i < 6; i++) {
            ImageView heart = new ImageView(heartIcon.getImage());
            heart.setFitWidth(24);
            heart.setFitHeight(24);
            // Get the center of the heart button in scene coordinates
            double startX = heartButton.localToScene(heartButton.getWidth()/2, heartButton.getHeight()/2).getX();
            double startY = heartButton.localToScene(heartButton.getWidth()/2, heartButton.getHeight()/2).getY();
            // Convert to effectsPane local coordinates
            double paneX = effectsPane.sceneToLocal(startX, startY).getX();
            double paneY = effectsPane.sceneToLocal(startX, startY).getY();
            heart.setLayoutX(paneX);
            heart.setLayoutY(paneY);
            effectsPane.getChildren().add(heart);
            // Random direction
            double angle = Math.toRadians(60 * i + 20 - Math.random()*40);
            double distance = 80 + Math.random()*30;
            double dx = Math.cos(angle) * distance;
            double dy = Math.sin(angle) * distance;
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(heart.opacityProperty(), 1),
                    new KeyValue(heart.translateXProperty(), 0),
                    new KeyValue(heart.translateYProperty(), 0),
                    new KeyValue(heart.scaleXProperty(), 1),
                    new KeyValue(heart.scaleYProperty(), 1)
                ),
                new KeyFrame(Duration.seconds(0.7),
                    new KeyValue(heart.opacityProperty(), 0),
                    new KeyValue(heart.translateXProperty(), dx),
                    new KeyValue(heart.translateYProperty(), dy),
                    new KeyValue(heart.scaleXProperty(), 1.5),
                    new KeyValue(heart.scaleYProperty(), 1.5)
                )
            );
            tl.setOnFinished(e -> effectsPane.getChildren().remove(heart));
            tl.play();
        }
    }

    private void updateAvailableMicrophones() {
        if (micSelector == null) return;
        
        micSelector.getItems().clear();
        Mixer.Info[] mixers = AudioStreamUtils.getAvailableMixers();
        for (Mixer.Info mixer : mixers) {
            micSelector.getItems().add(mixer.getName());
        }
    }

    private void setSelectedMicrophone() {
        if (micSelector == null) return;
        
        String selected = micSelector.getValue();
        if (selected != null) {
            Mixer.Info[] mixers = AudioStreamUtils.getAvailableMixers();
            for (Mixer.Info mixer : mixers) {
                if (mixer.getName().equals(selected)) {
                    AudioStreamUtils.setSelectedMixer(mixer);
                    selectedMicrophone = mixer;
                    System.out.println("Selected microphone: " + mixer.getName());
                    
                    // If we're currently streaming, restart with the new microphone
                    if (isAudioStreaming && client != null) {
                        client.stopAudioStreaming();
                        client.startAudioStreaming();
                    }
                    break;
                }
            }
        }
    }

    private void startAudioLevelMeter() {
        if (audioLevelMeter == null) return;
        
        if (audioLevelTimer != null) {
            audioLevelTimer.stop();
        }
        
        audioLevelTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                float level = AudioStreamUtils.getCurrentInputLevel();
                Platform.runLater(() -> {
                    audioLevelMeter.setProgress(level);
                    // Change color based on level
                    if (level > 0.7) {
                        audioLevelMeter.setStyle("-fx-accent: red;");
                    } else if (level > 0.3) {
                        audioLevelMeter.setStyle("-fx-accent: yellow;");
                    } else {
                        audioLevelMeter.setStyle("-fx-accent: green;");
                    }
                });
            }
        };
        audioLevelTimer.start();
    }

    private void updateVolumeMeter(byte[] audioData) {
        if (audioLevelMeter == null) return;
        
        double volume = AudioStreamUtils.calculateVolume(audioData);
        Platform.runLater(() -> {
            audioLevelMeter.setProgress(volume);
            // Change color based on volume level
            if (volume > 0.7) {
                audioLevelMeter.setStyle("-fx-accent: red;");
            } else if (volume > 0.3) {
                audioLevelMeter.setStyle("-fx-accent: yellow;");
            } else {
                audioLevelMeter.setStyle("-fx-accent: green;");
            }
        });
    }

    private void toggleAudioStreaming() {
        if (client != null) {
            if (!isAudioStreaming) {
                client.startAudioStreaming();
                isAudioStreaming = true;
            } else {
                client.stopAudioStreaming();
                isAudioStreaming = false;
            }
            updateAudioButtonState();
        }
    }

    private void updateAudioButtonState() {
        if (audioButton != null && audioOnIcon != null && audioOffIcon != null) {
            audioOnIcon.setVisible(isAudioStreaming);
            audioOffIcon.setVisible(!isAudioStreaming);
        }
    }

    private void disconnect() {
        if (client != null) {
            if (isAudioStreaming) {
                client.stopAudioStreaming();
                isAudioStreaming = false;
            }
            client.stop();
        }
        
        // Stop the audio level timer if it's running
        if (audioLevelTimer != null) {
            audioLevelTimer.stop();
        }
        
        // Make sure we clean up any remaining audio resources
        AudioStreamUtils.stopAudioStream();
        AudioStreamUtils.stopPlayback();
    }
    
    public void stop() {
        disconnect();
    }
} 