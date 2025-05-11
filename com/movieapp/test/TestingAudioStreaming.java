package com.movieapp.test;

import com.movieapp.network.Server;
import com.movieapp.network.Client;
import com.movieapp.utils.AudioStreamUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import java.util.concurrent.ScheduledExecutorService;

public class TestingAudioStreaming extends Application {
    private Server server;
    private Client client;
    private ProgressBar volumeMeter;
    private Label micInfo;
    private ComboBox<String> micSelector;
    private Button testButton;
    private boolean isTesting = false;
    private ScheduledExecutorService executor;
    private Mixer.Info selectedMicrophone;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Server controls
        Button startServerBtn = new Button("Start Server");
        startServerBtn.setOnAction(e -> startServer());

        // Client controls
        Button startClientBtn = new Button("Start Client");
        startClientBtn.setOnAction(e -> startClient());

        // Microphone selection
        micSelector = new ComboBox<>();
        micSelector.setPromptText("Select Microphone");
        updateMicInfo(micSelector);
        micSelector.setOnAction(e -> setSelectedMic());

        // Volume meter
        volumeMeter = new ProgressBar(0);
        volumeMeter.setPrefWidth(200);
        volumeMeter.setStyle("-fx-accent: green;");

        // Microphone info
        micInfo = new Label("No microphone selected");

        // Test button
        testButton = new Button("Test Microphone");
        testButton.setOnAction(e -> testMicrophone());
        testButton.setDisable(true);

        // Add speaker test button
        Button testSpeakersButton = new Button("Test Speakers");
        testSpeakersButton.setOnAction(e -> testSpeakers());

        root.getChildren().addAll(
            startServerBtn,
            startClientBtn,
            micSelector,
            volumeMeter,
            micInfo,
            testButton,
            testSpeakersButton
        );

        Scene scene = new Scene(root, 300, 400);
        primaryStage.setTitle("Audio Streaming Test");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start volume meter
        startVolumeMeter();
    }

    private void startServer() {
        try {
            server = new Server();
            server.start(5555);
        } catch (Exception e) {
            System.err.println("[Server] Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startClient() {
        client = new Client(new Client.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                System.out.println("[Client] " + message);
            }

            @Override
            public void onFileReceived(com.movieapp.model.FileTransfer fileTransfer) {}

            @Override
            public void onConnectionClosed() {
                Platform.runLater(() -> {
                    testButton.setDisable(true);
                    micSelector.setDisable(true);
                });
            }

            @Override
            public void onImageReceived(javafx.scene.image.Image image, String name) {}

            @Override
            public void onAudioReceived(byte[] audioData, String senderId) {
                AudioStreamUtils.playAudioStream(audioData);
            }
        });

        try {
            client.connectToHost("localhost", 5555);
            Platform.runLater(() -> {
                testButton.setDisable(false);
                micSelector.setDisable(false);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMicInfo(ComboBox<String> selector) {
        selector.getItems().clear();
        Mixer.Info[] mixers = AudioStreamUtils.getAvailableMixers();
        for (Mixer.Info mixer : mixers) {
            selector.getItems().add(mixer.getName());
        }
    }

    private void setSelectedMic() {
        String selected = micSelector.getValue();
        if (selected != null) {
            Mixer.Info[] mixers = AudioStreamUtils.getAvailableMixers();
            for (Mixer.Info mixer : mixers) {
                if (mixer.getName().equals(selected)) {
                    try {
                        Mixer m = AudioSystem.getMixer(mixer);
                        Line.Info[] targetLines = m.getTargetLineInfo();
                        if (targetLines.length > 0) {
                            AudioStreamUtils.setSelectedMixer(mixer);
                            micInfo.setText("Selected: " + mixer.getName() + "\nType: " + targetLines[0].getClass().getSimpleName());
                            selectedMicrophone = mixer;
                            testButton.setDisable(false);
                        } else {
                            micInfo.setText("Error: Selected device is not a microphone");
                            testButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        micInfo.setText("Error: " + e.getMessage());
                        testButton.setDisable(true);
                    }
                    break;
                }
            }
        }
    }

    private void testSpeakers() {
        System.out.println("Testing speakers...");
        AudioStreamUtils.testSpeakers();
    }

    private void testMicrophone() {
        if (selectedMicrophone == null) {
            System.err.println("No microphone selected!");
            return;
        }

        if (isTesting) {
            stopTest();
        } else {
            startTest();
        }
    }

    private void startTest() {
        isTesting = true;
        testButton.setText("Stop Test");
        System.out.println("Testing microphone: " + selectedMicrophone.getName());
        
        AudioStreamUtils.startAudioStream(pcmBytes -> {
            System.out.println("Captured audio: " + pcmBytes.length + " bytes");
            updateVolumeMeter(pcmBytes);
            // Play the audio back through speakers
            AudioStreamUtils.playAudioStream(pcmBytes);
        });
    }

    private void stopTest() {
        isTesting = false;
        testButton.setText("Test Microphone");
        AudioStreamUtils.stopAudioStream();
        volumeMeter.setProgress(0);
        volumeMeter.setStyle("-fx-accent: green;");
    }

    private void startVolumeMeter() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isTesting) {
                    float level = AudioStreamUtils.getCurrentInputLevel();
                    volumeMeter.setProgress(level);
                    if (level > 0.1) {
                        volumeMeter.setStyle("-fx-accent: red;");
                    } else {
                        volumeMeter.setStyle("-fx-accent: green;");
                    }
                }
            }
        };
        timer.start();
    }

    private void updateVolumeMeter(byte[] audioData) {
        double volume = AudioStreamUtils.calculateVolume(audioData);
        Platform.runLater(() -> {
            volumeMeter.setProgress(volume);
            // Change color based on volume level
            if (volume > 0.7) {
                volumeMeter.setStyle("-fx-accent: red;");
            } else if (volume > 0.3) {
                volumeMeter.setStyle("-fx-accent: yellow;");
            } else {
                volumeMeter.setStyle("-fx-accent: green;");
            }
        });
    }

    @Override
    public void stop() {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 