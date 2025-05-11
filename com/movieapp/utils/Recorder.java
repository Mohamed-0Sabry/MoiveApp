package com.movieapp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Recorder {
    private static final int FRAME_RATE = 30;
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNELS = 2;
    private static final String FORMAT = "mp4";
    private static final String DEFAULT_RECORDINGS_DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "MovieApp Recordings";

    private Process ffmpegProcess;
    private volatile boolean running = false;
    private String outputFile;
    private String ffmpegPath;
    private String recordingsDir;

    public Recorder() {
        this(DEFAULT_RECORDINGS_DIR);
    }

    public Recorder(String recordingsDirectory) {
        this.recordingsDir = recordingsDirectory;
        
        // Try to find ffmpeg in common locations
        String[] possiblePaths = {
            "ffmpeg",  // If it's in PATH
            "C:\\ffmpeg\\bin\\ffmpeg.exe",
            "C:\\Program Files\\ffmpeg\\bin\\ffmpeg.exe",
            "C:\\Program Files (x86)\\ffmpeg\\bin\\ffmpeg.exe"
        };

        for (String path : possiblePaths) {
            if (isFFmpegAvailable(path)) {
                ffmpegPath = path;
                break;
            }
        }

        if (ffmpegPath == null) {
            throw new RuntimeException(
                "FFmpeg not found! Please install FFmpeg and either:\n" +
                "1. Add it to your system PATH, or\n" +
                "2. Set the path manually using setFFmpegPath()"
            );
        }
    }

    /**
     * Set the path to the FFmpeg executable
     * @param path Full path to ffmpeg.exe
     */
    public void setFFmpegPath(String path) {
        if (isFFmpegAvailable(path)) {
            this.ffmpegPath = path;
        } else {
            throw new IllegalArgumentException("FFmpeg not found at: " + path);
        }
    }

    private boolean isFFmpegAvailable(String path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(path, "-version");
            Process p = pb.start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Starts recording screen + mic + system audio.
     */
    public void startRecording() throws Exception {
        if (running) return;
        running = true;

        // Create recordings directory if it doesn't exist
        Path recordingsPath = Paths.get(recordingsDir);
        if (!Files.exists(recordingsPath)) {
            try {
                Files.createDirectories(recordingsPath);
                System.out.println("Created recordings directory: " + recordingsPath.toAbsolutePath());
            } catch (IOException e) {
                throw new Exception("Failed to create recordings directory: " + recordingsPath, e);
            }
        }

        // Generate output filename
        outputFile = recordingsPath.resolve("rec_" + System.currentTimeMillis() + ".mp4").toString();
        System.out.println("Will save recording to: " + outputFile);

        // Build FFmpeg command
        String[] command = {
            ffmpegPath,
            "-f", "dshow",
            "-i", "video=desktop:audio=@device_cm_{33D9A762-90C8-11D0-BD43-00A0C911CE86}\\wave_{595CE12C-BD3A-4E5C-B3F2-81137B01FC47}",
            "-framerate", String.valueOf(FRAME_RATE),
            "-video_size", "1920x1080",
            "-pix_fmt", "yuv420p",
            "-c:v", "libx264",
            "-preset", "ultrafast",
            "-crf", "23",
            "-c:a", "aac",
            "-b:a", "192k",
            "-ar", String.valueOf(AUDIO_SAMPLE_RATE),
            "-ac", String.valueOf(AUDIO_CHANNELS),
            "-y",  // Overwrite output file if exists
            outputFile
        };

        // Start FFmpeg process
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        try {
            System.out.println("Starting FFmpeg with command: " + String.join(" ", command));
            ffmpegProcess = processBuilder.start();
            
            // Start a thread to monitor the process
            new Thread(() -> {
                try {
                    ffmpegProcess.waitFor();
                    running = false;
                    // Verify file was created
                    File output = new File(outputFile);
                    if (output.exists() && output.length() > 0) {
                        System.out.println("Recording completed successfully. File saved to: " + outputFile);
                    } else {
                        System.err.println("Warning: Recording file was not created or is empty: " + outputFile);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "FFmpeg-Monitor").start();

        } catch (IOException e) {
            running = false;
            throw new Exception("Failed to start FFmpeg: " + e.getMessage() + 
                "\nPlease make sure FFmpeg is installed and the path is correct: " + ffmpegPath, e);
        }
    }

    /** Stops the ongoing recording. */
    public void stopRecording() {
        if (!running) return;
        
        running = false;
        if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
            ffmpegProcess.destroy();
            try {
                // Give FFmpeg a moment to finish writing
                Thread.sleep(1000);
                
                // Verify file was created
                File output = new File(outputFile);
                if (output.exists() && output.length() > 0) {
                    System.out.println("Recording saved in: " + output.getParent());
                } else {
                    System.err.println("Warning: Recording file was not created or is empty: " + outputFile);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** @return true if currently recording. */
    public boolean isRecording() {
        return running;
    }

    /** @return the path to the current recording file. */
    public String getOutputFile() {
        return outputFile;
    }

    /** @return the recordings directory path. */
    public String getRecordingsDirectory() {
        return recordingsDir;
    }
}
