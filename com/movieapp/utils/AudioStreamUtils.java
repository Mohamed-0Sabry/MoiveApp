package com.movieapp.utils;

import javax.sound.sampled.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;

public class AudioStreamUtils {
    private static final float SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE = 16, CHANNELS = 1, BUFFER_SIZE = 4096;
    private static final boolean SIGNED = true, BIG_ENDIAN = false;
    
    private static TargetDataLine mic;
    private static SourceDataLine speaker;
    private static AtomicBoolean isStreaming = new AtomicBoolean(false);
    private static Thread captureThread;
    private static AudioStreamListener listener;
    private static ExecutorService audioExecutor;
    private static Mixer selectedMixer;
    private static AudioFormat audioFormat;
    private static float currentInputLevel = 0.0f, currentOutputLevel = 0.0f;
    private static SourceDataLine persistentSpeaker;

    public interface AudioStreamListener { void onAudioData(byte[] audioData); }

    public static Mixer.Info[] getAvailableMixers() {
        return java.util.Arrays.stream(AudioSystem.getMixerInfo())
            .filter(mixerInfo -> AudioSystem.getMixer(mixerInfo).getTargetLineInfo().length > 0)
            .toArray(Mixer.Info[]::new);
    }

    public static void setSelectedMixer(Mixer.Info mixerInfo) {
        try {
            selectedMixer = AudioSystem.getMixer(mixerInfo);
            System.out.println("[Audio] Selected mixer: " + mixerInfo.getName());
        } catch (Exception e) {
            System.err.println("[Audio] Error setting mixer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void startAudioStream(AudioStreamListener streamListener) {
        if (isStreaming.get()) stopAudioStream();
        listener = streamListener;
        isStreaming.set(true);
        if (audioExecutor != null) audioExecutor.shutdownNow();
        audioExecutor = Executors.newSingleThreadExecutor();

        try {
            audioFormat = getAudioFormat();
            System.out.println("[Audio] Using format: " + audioFormat);
            
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            mic = selectedMixer == null ? (TargetDataLine) AudioSystem.getLine(micInfo) 
                                       : (TargetDataLine) selectedMixer.getLine(micInfo);
            mic.open(audioFormat, BUFFER_SIZE);
            mic.start();
            System.out.println("[Audio] Microphone started");

            captureThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (isStreaming.get()) {
                    try {
                        int bytesRead = mic.read(buffer, 0, buffer.length);
                        if (bytesRead > 0 && listener != null) {
                            byte[] audioData = new byte[bytesRead];
                            System.arraycopy(buffer, 0, audioData, 0, bytesRead);
                            updateInputLevel(audioData);
                            listener.onAudioData(audioData);
                        }
                    } catch (Exception e) {
                        if (isStreaming.get()) {
                            System.err.println("[Audio] Error reading from microphone: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });
            captureThread.start();
            System.out.println("[Audio] Capture thread started");
        } catch (Exception e) {
            System.err.println("[Audio] Error starting audio stream: " + e.getMessage());
            e.printStackTrace();
            stopAudioStream();
        }
    }

    public static synchronized void stopAudioStream() {
        System.out.println("[Audio] Stopping audio stream...");
        isStreaming.set(false);
        if (mic != null) {
            try { mic.stop(); mic.close(); System.out.println("[Audio] Microphone closed"); } 
            catch (Exception e) { System.err.println("[Audio] Error closing microphone: " + e.getMessage()); }
            mic = null;
        }
        if (captureThread != null) {
            captureThread.interrupt();
            try { captureThread.join(100); System.out.println("[Audio] Capture thread stopped"); } 
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            captureThread = null;
        }
        if (audioExecutor != null) {
            audioExecutor.shutdownNow();
            audioExecutor = null;
            System.out.println("[Audio] Audio executor shutdown");
        }
    }

    public static synchronized void testSpeakers() {
        System.out.println("[Audio] Testing speakers...");
        if (audioExecutor == null || audioExecutor.isShutdown()) audioExecutor = Executors.newSingleThreadExecutor();
        audioExecutor.submit(() -> {
            try {
                byte[] testTone = generateTestTone();
                if (audioFormat == null) audioFormat = getAudioFormat();
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("[Audio] Speaker not supported with format: " + audioFormat);
                    return;
                }
                SourceDataLine testLine = (SourceDataLine) AudioSystem.getLine(info);
                testLine.open(audioFormat, BUFFER_SIZE);
                testLine.start();
                System.out.println("[Audio] Playing test tone...");
                testLine.write(testTone, 0, testTone.length);
                testLine.drain();
                testLine.close();
                System.out.println("[Audio] Test tone completed");
            } catch (Exception e) {
                System.err.println("[Audio] Error testing speakers: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static byte[] generateTestTone() {
        int numSamples = (int) (SAMPLE_RATE * 2);
        byte[] tone = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            short sample = (short) (Short.MAX_VALUE * Math.sin(i / (SAMPLE_RATE / 440.0) * 2.0 * Math.PI));
            tone[i * 2] = (byte) (sample & 0xFF);
            tone[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return tone;
    }

    public static synchronized void playAudioStream(byte[] audioData) {
        if (audioExecutor == null || audioExecutor.isShutdown()) audioExecutor = Executors.newSingleThreadExecutor();
        audioExecutor.submit(() -> {
            try {
                if (audioFormat == null) audioFormat = getAudioFormat();
                if (persistentSpeaker == null || !persistentSpeaker.isOpen() || !persistentSpeaker.isActive()) {
                    if (persistentSpeaker != null) try { persistentSpeaker.close(); } 
                    catch (Exception e) { System.err.println("[Audio] Error closing existing speaker: " + e.getMessage()); }
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                    if (!AudioSystem.isLineSupported(info)) {
                        System.err.println("[Audio] Speaker not supported with format: " + audioFormat);
                        return;
                    }
                    persistentSpeaker = (SourceDataLine) AudioSystem.getLine(info);
                    persistentSpeaker.open(audioFormat, BUFFER_SIZE);
                    persistentSpeaker.start();
                    System.out.println("[Audio] Speaker initialized");
                }
                persistentSpeaker.write(audioData, 0, audioData.length);
                updateOutputLevel(audioData);
            } catch (Exception e) {
                System.err.println("[Audio] Error playing audio: " + e.getMessage());
                e.printStackTrace();
                try {
                    if (persistentSpeaker != null) { persistentSpeaker.stop(); persistentSpeaker.close(); persistentSpeaker = null; }
                } catch (Exception ex) { System.err.println("[Audio] Error recovering speaker: " + ex.getMessage()); }
            }
        });
    }

    public static synchronized void stopPlayback() {
        if (persistentSpeaker != null) {
            try { persistentSpeaker.stop(); persistentSpeaker.flush(); persistentSpeaker.close(); System.out.println("[Audio] Speaker closed"); } 
            catch (Exception e) { System.err.println("[Audio] Error closing speaker: " + e.getMessage()); } 
            finally { persistentSpeaker = null; }
        }    
    }

    public static float getCurrentInputLevel() { return currentInputLevel; }
    public static float getCurrentOutputLevel() { return currentOutputLevel; }

    private static void updateInputLevel(byte[] audioData) { currentInputLevel = calculateLevel(audioData); }
    private static void updateOutputLevel(byte[] audioData) { currentOutputLevel = calculateLevel(audioData); }
    public static void processAudioInput(byte[] audioData) { updateInputLevel(audioData); }
    public static void processAudioOutput(byte[] audioData) { updateOutputLevel(audioData); }

    public static AudioFormat getAudioFormat() {
        return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
    }

    public static double calculateVolume(byte[] audioData) {
        if (audioData == null || audioData.length == 0) return 0.0;
        double sum = 0;
        for (int i = 0; i < audioData.length; i += 2) {
            if (i + 1 < audioData.length) {
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                sum += sample * sample;
            }
        }
        return Math.min(1.0, Math.sqrt(sum / (audioData.length / 2)) / Short.MAX_VALUE);
    }

    private static float calculateLevel(byte[] audioData) {
        if (audioData == null || audioData.length == 0) return 0.0f;
        float sum = 0;
        for (byte b : audioData) sum += b * b;
        return Math.min(1.0f, (float)(Math.sqrt(sum / audioData.length) / 64.0f * 2.0f));
    }
}