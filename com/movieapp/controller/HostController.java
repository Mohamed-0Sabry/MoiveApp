package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.control.Slider;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import javax.imageio.ImageIO;
import com.movieapp.network.Client;
import com.movieapp.network.Server;
import com.movieapp.model.FileTransfer;
import com.movieapp.utils.AudioStreamUtils;
import com.movieapp.utils.StageManager;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.ToggleButton;
import com.movieapp.utils.RecordEverythingController;
import com.movieapp.controller.effects.HeartEffectsController;
import com.movieapp.controller.audio.AudioController;

/**
 * Controller for the Host Screen. Handles screen capture, chat, audio, and recording.
 */
public class HostController {
    // FXML-injected fields
    @FXML private ImageView screenImageView;
    @FXML private HBox mediaContainer;
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
    @FXML private StackPane chatPanel;
    @FXML private ToggleButton audioButton;
    @FXML private ImageView audioOnIcon;
    @FXML private ImageView audioOffIcon;
    @FXML private ComboBox<String> micSelector;
    @FXML private ToggleButton recordButton;
    @FXML private ImageView recordIcon;
    @FXML private ImageView stopIcon;

    // Controllers
    private HeartEffectsController heartEffectsController;
    private AudioController audioController;
    private RecordEverythingController recorder = new RecordEverythingController();

    // State fields
    private AnimationTimer captureTimer;
    private boolean capturing = false;
    private Robot robot;
    private Stage primaryStage;
    private Client client;
    private Server server;
    private static final int DEFAULT_PORT = 5555;
    private ChatController chatController;
    private boolean isChatOpen = false;
    private String currentRecordingPath = null;
    private boolean isAudioStreaming = false;

    /**
     * Initialize controller and set up primary stage reference.
     */
    @FXML
    private void initialize() {
        // Get stage reference when scene is available
        rootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                primaryStage = (Stage) newValue.getWindow();
            }
        });

        // Start server first
        try {
            server = new Server();
            server.start(DEFAULT_PORT);
            System.out.println("Server started successfully on port " + DEFAULT_PORT);
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize chat panel and client
        initializeClient();
        initializeChatPanel();

        // Setup screen capture
        try {
            System.setProperty("java.awt.headless", "false");
            robot = new Robot();
            
            if (screenImageView != null) {
                screenImageView.setPreserveRatio(true);
                screenImageView.fitWidthProperty().bind(mediaContainer.widthProperty());
                screenImageView.fitHeightProperty().bind(mediaContainer.heightProperty());
            }
            
            startScreenCapture();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // Initialize controllers
        heartEffectsController = new HeartEffectsController(heartButton, heartIcon, effectsPane, client);
        
        // Setup recorder state listener
        recorder.setStateListener((isRecording, filePath) -> {
            Platform.runLater(() -> {
                if (isRecording) {
                    currentRecordingPath = filePath;
                    recordButton.setSelected(true);
                    if (stopIcon != null && recordIcon != null) {
                        recordIcon.setVisible(false);
                        stopIcon.setVisible(true);
                    }
                } else {
                    currentRecordingPath = null;
                    recordButton.setSelected(false);
                    if (stopIcon != null && recordIcon != null) {
                        recordIcon.setVisible(true);
                        stopIcon.setVisible(false);
                    }
                }
            });
        });
        
        // Initialize volume controls
        setupVolumeControls();
        
        // Make effectsPane mouse transparent so it doesn't block button clicks
        if (effectsPane != null) {
            effectsPane.setMouseTransparent(true);
        }
    }

    private void initializeClient() {
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
                    System.out.println("Connection to server closed");
                    if (isAudioStreaming) {
                        stopAudioStreaming();
                    }
                });
            }
            
            @Override
            public void onImageReceived(Image image, String name) {
                Platform.runLater(() -> {
                    if (name.equals("screen")) {
                        // This is a screen share frame
                        if (screenImageView != null) {
                            screenImageView.setImage(image);
                        }
                    } else if (chatController != null) {
                        // This is a chat image
                        chatController.onImageReceived(image, name);
                    }
                });
            }
            
            @Override
            public void onAudioReceived(byte[] audioData, String senderId) {
                AudioStreamUtils.playAudioStream(audioData);
                if (audioController != null) {
                    audioController.updateVolumeMeter(audioData);
                }
            }
            
            @Override
            public void onHeartAnimation(String username, boolean isLiked) {
                Platform.runLater(() -> {
                    // Only show the floating heart for remote triggers
                    if (heartEffectsController != null) {
                        heartEffectsController.setLiked(isLiked);
                        heartEffectsController.showFloatingHeart();
                    }
                });
            }
        });

        try {
            client.connectToHost(Client.findHost(8888, 2000), DEFAULT_PORT);
            System.out.println("Successfully connected to chat server");
        } catch (Exception e) {
            System.err.println("Error connecting to chat server: " + e.getMessage());
        }
    }

    private void setupVolumeControls() {
        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (volumeOnIcon != null && volumeMuteIcon != null) {
                    boolean isMuted = newVal.doubleValue() == 0;
                    volumeOnIcon.setVisible(!isMuted);
                    volumeMuteIcon.setVisible(isMuted);
                }
            });
        }
        
        if (muteButton != null) {
            muteButton.setOnAction(event -> toggleMute());
        }
    }

    private void toggleMute() {
        volumeSlider.setValue(volumeSlider.getValue() == 0 ? 100 : 0);
    }

    private void initializeChatPanel() {
        try {
            // Initialize audio controller after client is ready
            audioController = new AudioController(audioButton, audioOnIcon, audioOffIcon, null, micSelector, client);
            
            // Load the sliding chat panel
            StageManager.getInstance().loadSlidingPanel(rootPane, "/com/movieapp/view/DemoThemeServer.fxml", "/com/movieapp/styles/css-stylesheet.css");
            
            // Get reference to the chat panel
            chatPanel = (StackPane) rootPane.getChildren().get(rootPane.getChildren().size() - 1);
            
            // Get the chat controller
            chatController = (ChatController) chatPanel.getUserData();
            if (chatController != null) {
                chatController.setClient(client);
            } else {
                System.err.println("Failed to get chat controller!");
            }
            
            // Make sure chat panel is initially hidden but in front when needed
            chatPanel.setVisible(false);
            chatPanel.toFront();
        } catch (Exception e) {
            System.err.println("Error initializing chat panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackButtonClicked() {
        try {
            stop(); // Make sure to clean up resources
            com.movieapp.Main.switchToMainScreen();
        } catch (Exception e) {
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
            
            // Send to clients
            if (client != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(awtImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                // Ensure proper base64 encoding
                String base64 = Base64.getEncoder().withoutPadding().encodeToString(imageBytes);
                client.sendMessage("FRAME:" + base64);
            }
            
            // Display locally
            WritableImage fxImage = new WritableImage(awtImage.getWidth(), awtImage.getHeight());
            PixelGrabber pg = new PixelGrabber(
                awtImage, 0, 0, awtImage.getWidth(), awtImage.getHeight(),
                ((DataBufferInt) awtImage.getRaster().getDataBuffer()).getData(), 0, awtImage.getWidth()
            );
            
            pg.grabPixels();
            
            fxImage.getPixelWriter().setPixels(0, 0, awtImage.getWidth(), awtImage.getHeight(),
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                ((DataBufferInt) awtImage.getRaster().getDataBuffer()).getData(),
                0, awtImage.getWidth()
            );
            
            Platform.runLater(() -> {
                if (screenImageView != null) {
                    screenImageView.setImage(fxImage);
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
    private void onChatButtonClicked() {
        isChatOpen = !isChatOpen;
        chatPanel.setVisible(true);
        StageManager.getInstance().showSlidingPanel(chatPanel, isChatOpen);
        
        if (isChatOpen) {
            chatPanel.toFront();
            Platform.runLater(() -> {
                if (chatController != null) {
                    chatController.focusMessageField();
                }
            });
        }    
    }

    public boolean getChatState() {
        return isChatOpen;
    }

    @FXML
    private void onRecordButtonClicked() {
        try {
            String selectedMic = micSelector != null ? micSelector.getValue() : null;
            if ((selectedMic == null || selectedMic.isEmpty()) && micSelector != null && !micSelector.getItems().isEmpty()) {
                selectedMic = micSelector.getItems().get(0);
            }
            
            String systemDevice = "Stereo Mix (Realtek(R) Audio)"; // Hardcoded for now, can be made dynamic
            
            if (recordButton.isSelected()) {
                // Starting recording with selected microphone and system audio
                recorder.toggleRecording(selectedMic, systemDevice);
            } else {
                // Stopping recording
                recorder.toggleRecording(selectedMic, systemDevice);
                // Show where the file was saved
                String userHome = System.getProperty("user.home");
                String recordingsPath = new File(userHome, "Documents/MovieApp Recordings").getAbsolutePath();
                System.out.println("Recording saved in: " + recordingsPath);
            }
        } catch (Exception e) {
            System.err.println("Recording error: " + e.getMessage());
            recordButton.setSelected(false);
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
        if (audioController != null) {
            audioController.stop();
        }
        if (isAudioStreaming) {
            stopAudioStreaming();
        }
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