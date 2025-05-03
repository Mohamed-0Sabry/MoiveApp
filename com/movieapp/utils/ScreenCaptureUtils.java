package com.movieapp.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenCaptureUtils {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Robot robot;
    private static Rectangle screenRect;
    private static volatile boolean isCapturing = false;

    static {
        try {
            robot = new Robot();
            screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        } catch (AWTException e) {
            System.err.println("Error initializing screen capture: " + e.getMessage());
        }
    }

    public static byte[] captureScreen() {
        try {
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            return compressImage(screenCapture);
        } catch (Exception e) {
            System.err.println("Error capturing screen: " + e.getMessage());
            return new byte[0];
        }
    }

    public static void startScreenCapture(ScreenCaptureListener listener, int fps) {
        if (isCapturing) return;
        
        isCapturing = true;
        scheduler.scheduleAtFixedRate(() -> {
            if (isCapturing) {
                byte[] frame = captureScreen();
                if (frame.length > 0) {
                    listener.onFrameCaptured(frame);
                }
            }
        }, 0, 1000 / fps, TimeUnit.MILLISECONDS);
    }

    public static void stopScreenCapture() {
        isCapturing = false;
    }

    private static byte[] compressImage(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Error compressing image: " + e.getMessage());
            return new byte[0];
        }
    }

    public interface ScreenCaptureListener {
        void onFrameCaptured(byte[] frameData);
    }
} 