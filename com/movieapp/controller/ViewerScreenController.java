package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import java.io.File;

import com.movieapp.network.Client;
import com.movieapp.model.FileTransfer;
import com.movieapp.utils.AudioStreamUtils;
import com.movieapp.utils.StageManager;
import javafx.application.Platform;
import javafx.scene.control.ToggleButton;
import com.movieapp.utils.RecordEverythingController;
import com.movieapp.controller.effects.HeartEffectsController;
import com.movieapp.controller.audio.AudioController;
import com.movieapp.controller.fullscreen.FullscreenController;

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
    @FXML private ToggleButton recordButton;

    // Controllers
    private HeartEffectsController heartEffectsController;
    private AudioController audioController;
    private FullscreenController fullscreenController;
    private RecordEverythingController recorder = new RecordEverythingController();

    // State fields
    private Stage primaryStage;
    private Client client;
    private ChatController chatController;
    private boolean isChatOpen = false;
    private String currentRecordingPath = null;

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

        // Initialize controllers
        heartEffectsController = new HeartEffectsController(heartButton, heartIcon, effectsPane);
        fullscreenController = new FullscreenController(mediaView, rootPane, controlsPane, topBar, heartButtonContainer, effectsPane);

        // Setup recorder state listener
        recorder.setStateListener((isRecording, filePath) -> {
            Platform.runLater(() -> {
                if (isRecording) {
                    currentRecordingPath = filePath;
                    recordButton.setSelected(true);
                } else {
                    currentRecordingPath = null;
                    recordButton.setSelected(false);
                }
            });
        });
        
        // Initialize volume controls
        setupVolumeControls();
        
        // Make effectsPane mouse transparent so it doesn't block button clicks
        if (effectsPane != null) {
            effectsPane.setMouseTransparent(true);
        }

        // Initialize chat panel
        initializeChatPanel();
    }

    private void setupVolumeControls() {
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaView.getMediaPlayer() != null) {
                double volume = newVal.doubleValue() / 100.0;
                mediaView.getMediaPlayer().setVolume(volume);
            }
            if (volumeOnIcon != null && volumeMuteIcon != null) {
                boolean isMuted = newVal.doubleValue() == 0;
                volumeOnIcon.setVisible(!isMuted);
                volumeMuteIcon.setVisible(isMuted);
            }
        });

        muteButton.setOnAction(event -> toggleMute());
    }

    private void toggleMute() {
        volumeSlider.setValue(volumeSlider.getValue() == 0 ? 100 : 0);
    }

    private void initializeChatPanel() {
        try {
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
                    AudioStreamUtils.playAudioStream(audioData);
                    if (audioController != null) {
                        audioController.updateVolumeMeter(audioData);
                    }
                }
            });

            try {
                client.connectToHost("localhost", 5555);
                System.out.println("Successfully connected to chat server");
            } catch (Exception e) {
                System.err.println("Error connecting to chat server: " + e.getMessage());
                return;
            }

            audioController = new AudioController(audioButton, audioOnIcon, audioOffIcon, 
                                                audioLevelMeter, micSelector, client);

            StageManager.getInstance().loadSlidingPanel(rootPane, "/com/movieapp/view/DemoThemeServer.fxml", "/com/movieapp/styles/css-stylesheet.css");
            chatPanel = (StackPane) rootPane.getChildren().get(rootPane.getChildren().size() - 1);
            
            chatController = (ChatController) chatPanel.getUserData();
            if (chatController != null) {
                chatController.setClient(client);
            } else {
                System.err.println("Failed to get chat controller!");
            }

            chatPanel.toFront();
        } catch (Exception e) {
            System.err.println("Error initializing chat panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onFullscreenClicked() {
        fullscreenController.toggleFullscreen();
    }

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
            if (recordButton.isSelected()) {
                recorder.toggleRecording(selectedMic);
            } else {
                recorder.toggleRecording(selectedMic);
                String userHome = System.getProperty("user.home");
                String recordingsPath = new File(userHome, "Documents/MovieApp Recordings").getAbsolutePath();
                System.out.println("Recording saved in: " + recordingsPath);
            }
        } catch (Exception e) {
            System.err.println("Recording error: " + e.getMessage());
            recordButton.setSelected(false);
        }
    }

    public void stop() {
        if (audioController != null) {
            audioController.stop();
        }
        if (client != null) {
            client.stop();
        }
    }
} 