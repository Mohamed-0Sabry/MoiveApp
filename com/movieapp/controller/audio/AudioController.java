package com.movieapp.controller.audio;

import com.movieapp.utils.AudioStreamUtils;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.animation.AnimationTimer;
import javax.sound.sampled.Mixer;
import com.movieapp.network.Client;

public class AudioController {
    private ToggleButton audioButton;
    private ImageView audioOnIcon;
    private ImageView audioOffIcon;
    private ProgressBar audioLevelMeter;
    private ComboBox<String> micSelector;
    private Client client;
    private boolean isAudioStreaming = false;
    private AnimationTimer audioLevelTimer;

    public AudioController(ToggleButton audioButton, ImageView audioOnIcon, ImageView audioOffIcon, 
                          ProgressBar audioLevelMeter, ComboBox<String> micSelector, Client client) {
        this.audioButton = audioButton;
        this.audioOnIcon = audioOnIcon;
        this.audioOffIcon = audioOffIcon;
        this.audioLevelMeter = audioLevelMeter;
        this.micSelector = micSelector;
        this.client = client;
        
        initialize();
    }

    private void initialize() {
        if (audioButton != null) {
            audioButton.setOnAction(event -> toggleAudioStreaming());
            updateAudioButtonState();
        }

        if (audioLevelMeter != null) {
            audioLevelMeter.setProgress(0);
            audioLevelMeter.setStyle("-fx-accent: green;");
            startAudioLevelMeter();
        }

        if (micSelector != null) {
            updateAvailableMicrophones();
            micSelector.setOnAction(e -> setSelectedMicrophone());
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
                    System.out.println("Selected microphone: " + mixer.getName());
                    
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
                    updateMeterColor(level);
                });
            }
        };
        audioLevelTimer.start();
    }

    private void updateMeterColor(double level) {
        if (level > 0.7) {
            audioLevelMeter.setStyle("-fx-accent: red;");
        } else if (level > 0.3) {
            audioLevelMeter.setStyle("-fx-accent: yellow;");
        } else {
            audioLevelMeter.setStyle("-fx-accent: green;");
        }
    }

    public void updateVolumeMeter(byte[] audioData) {
        if (audioLevelMeter == null) return;
        
        double volume = AudioStreamUtils.calculateVolume(audioData);
        Platform.runLater(() -> {
            audioLevelMeter.setProgress(volume);
            updateMeterColor(volume);
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

    public void stop() {
        if (isAudioStreaming && client != null) {
            client.stopAudioStreaming();
            isAudioStreaming = false;
        }
        
        if (audioLevelTimer != null) {
            audioLevelTimer.stop();
        }
        
        AudioStreamUtils.stopAudioStream();
        AudioStreamUtils.stopPlayback();
    }
} 