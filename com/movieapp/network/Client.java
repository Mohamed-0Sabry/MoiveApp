package com.movieapp.network;

import com.movieapp.model.FileTransfer;

import comm.demo.utils.ScreenCaptureUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.Optional;


public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executorService;
    private MessageListener messageListener;
    public boolean isConnected;
    private ImageView imageView; 

    public interface MessageListener {
        void onMessageReceived(String message);
        void onFileReceived(FileTransfer fileTransfer);
        void onConnectionClosed();
        void onImageReceived(Image image);
    }

    public Client(MessageListener listener) {
        this.messageListener = listener;
        this.executorService = Executors.newSingleThreadExecutor();
        this.isConnected = false;
    }


    public void startScreenSharing(int fps) {
    ScreenCaptureUtils.startScreenCapture(frame -> {
        String base64 = Base64.getEncoder().encodeToString(frame);
        sendMessage("FRAME:" + base64);
    }, fps);
}

public void stopScreenSharing() {
    ScreenCaptureUtils.stopScreenCapture();
}


    public void connectToHost(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        isConnected = true;
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("User");
            dialog.setTitle("Enter your name");
            dialog.setHeaderText("Welcome to MovieApp Chat");
            dialog.setContentText("Please enter your username:");
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                sendMessage("SET_NAME:" + name);
            });
        });        
        startListening();
    }

    public void startCameraStreaming() {
        new Thread(() -> {
            VideoCapture camera = new VideoCapture(0);
            Mat frame = new Mat();
    
            while (isConnected && camera.isOpened()) {
                if (camera.read(frame)) {
                    MatOfByte buffer = new MatOfByte();
                    Imgcodecs.imencode(".jpg", frame, buffer);
                    byte[] imageBytes = buffer.toArray();
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    sendMessage("FRAME:" + base64);
                }
    
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            camera.release();
        }).start();
    }
    
    private void startListening() {
        executorService.submit(() -> {
            try {
                while (isConnected) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    
                    if (message.startsWith("FILE:")) {
                        handleFileTransfer();
                    } else if (message.startsWith("FRAME:")) {
                        String base64 = message.substring(6);
                        byte[] data = Base64.getDecoder().decode(base64);
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        BufferedImage img = ImageIO.read(bis);
                        Image fxImage = SwingFXUtils.toFXImage(img, null);
                    
                        if (imageView != null) {
                            Platform.runLater(() -> imageView.setImage(fxImage));
                        }
                    } else if (message.startsWith("IMAGE:")) {
                        String base64 = message.substring(6);
                        byte[] data = Base64.getDecoder().decode(base64);
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        BufferedImage img = ImageIO.read(bis);
                        Image fxImage = SwingFXUtils.toFXImage(img, null);
                        
                        
                        Platform.runLater(() -> {
                            messageListener.onImageReceived(fxImage);
                        });
                    } else {
                        messageListener.onMessageReceived(message);
                    }                    
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            } finally {
                disconnect();
            }
        });
    }

    private void handleFileTransfer() throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            FileTransfer fileTransfer = (FileTransfer) ois.readObject();
            messageListener.onFileReceived(fileTransfer);
        } catch (ClassNotFoundException e) {
            System.err.println("Error deserializing file: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (isConnected) {
            out.println(message);
        }
    }

    public void requestFileDownload(String filename) {
        if (isConnected) {
            out.println("FILE_REQUEST:" + filename);
        }
    }

    private void disconnect() {
        isConnected = false;
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        messageListener.onConnectionClosed();
    }

    public void stop() {
        disconnect();
        executorService.shutdown();
    }
} 