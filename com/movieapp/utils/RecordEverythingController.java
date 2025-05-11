package com.movieapp.utils;

import java.io.IOException;
import java.io.File;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioSystem;

public class RecordEverythingController {
    private Process ffmpegProcess;
    private boolean isRecording = false;
    private RecordingStateListener stateListener;

    // UPDATE THIS with your actual window title!
    private static final String WINDOW_TITLE = "Movie Night - Viewer"; // Your window title
    private static final String OUTPUT_FILE = "MovieApp_Recording.mp4";
    private static final String FFMPEG_PATH = "ffmpeg"; // If not in PATH, use full path

    public interface RecordingStateListener {
        void onRecordingStateChanged(boolean isRecording, String filePath);
    }

    public void setStateListener(RecordingStateListener listener) {
        this.stateListener = listener;
    }

    public void toggleRecording(String micDevice) throws IOException {
        if (!isRecording) {
            // Fallback to first available mic if none is selected
            if (micDevice == null || micDevice.isEmpty()) {
                micDevice = getFirstAvailableMicrophone();
                if (micDevice == null) {
                    throw new IOException("No microphone found!");
                }
            }
            startRecording(micDevice);
        } else {
            stopRecording();
        }
    }

    private String getFirstAvailableMicrophone() {
        javax.sound.sampled.Mixer.Info[] mixers = javax.sound.sampled.AudioSystem.getMixerInfo();
        for (javax.sound.sampled.Mixer.Info mixerInfo : mixers) {
            try {
                javax.sound.sampled.Mixer mixer = javax.sound.sampled.AudioSystem.getMixer(mixerInfo);
                if (mixer.getTargetLineInfo().length > 0) {
                    return mixerInfo.getName();
                }
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private void startRecording(String micDevice) throws IOException {
        if (micDevice == null || micDevice.isEmpty()) {
            throw new IOException("No microphone selected!");
        }
        // Check if ffmpeg exists
        if (!isFFmpegAvailable()) {
            throw new IOException("FFmpeg not found! Please install FFmpeg and add it to your PATH.");
        }
        // Create recordings directory in Documents if it doesn't exist
        String userHome = System.getProperty("user.home");
        File recordingsDir = new File(userHome, "Documents/MovieApp Recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }
        // Create a unique filename with timestamp
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String outputPath = new File(recordingsDir, "recording_" + timestamp + ".mp4").getAbsolutePath();
        
        // Updated FFmpeg command to use desktop capture and improved settings
        String command = String.format(
            "%s -y -f gdigrab -framerate 30 -i desktop -f dshow -i audio=\"%s\" -c:v libx264 -preset ultrafast -crf 23 -c:a aac -b:a 128k \"%s\"",
            FFMPEG_PATH, micDevice, outputPath
        );
        
        try {
            // Start FFmpeg process
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe", "/c", command);
            processBuilder.redirectErrorStream(true);
            ffmpegProcess = processBuilder.start();
            
            // Monitor FFmpeg output in a separate thread
            new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(ffmpegProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[FFmpeg] " + line);
                    }
                } catch (Exception e) {
                    System.err.println("[FFmpeg] Error reading output: " + e.getMessage());
                }
            }).start();
            
            isRecording = true;
            if (stateListener != null) {
                stateListener.onRecordingStateChanged(true, outputPath);
            }
            System.out.println("Recording started... Saving to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error starting recording: " + e.getMessage());
            throw e;
        }
    }

    private void stopRecording() {
        if (ffmpegProcess != null) {
            try {
                // Send 'q' to FFmpeg to gracefully stop recording
                java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(ffmpegProcess.getOutputStream());
                writer.write("q");
                writer.flush();
                writer.close();
                
                // Wait for FFmpeg to finish
                ffmpegProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                
                // Force destroy if still running
                if (ffmpegProcess.isAlive()) {
                    ffmpegProcess.destroyForcibly();
                }
            } catch (Exception e) {
                System.err.println("Error stopping recording: " + e.getMessage());
                if (ffmpegProcess.isAlive()) {
                    ffmpegProcess.destroyForcibly();
                }
            } finally {
                ffmpegProcess = null;
                isRecording = false;
                if (stateListener != null) {
                    stateListener.onRecordingStateChanged(false, null);
                }
                System.out.println("Recording stopped.");
            }
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    private boolean isFFmpegAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(FFMPEG_PATH + " -version");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}