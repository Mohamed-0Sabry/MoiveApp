package com.movieapp.network;

import com.movieapp.model.FileTransfer;
import com.movieapp.model.AudioPacket;
import com.movieapp.utils.ScreenCaptureUtils;
import com.movieapp.utils.AudioStreamUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.util.Enumeration;

public class Client {
private Socket socket;
private BufferedReader in;
private PrintWriter out;
private ExecutorService executorService;
private MessageListener messageListener;
private boolean isConnected;
private ImageView imageView;
private String clientUsername;
private DatagramSocket audioSocket;
private int serverAudioPort = 5556;
private ExecutorService audioExecutor;

public interface MessageListener {
    void onMessageReceived(String message);
    void onFileReceived(FileTransfer fileTransfer);
    void onConnectionClosed();
    void onImageReceived(Image image, String name);
    void onAudioReceived(byte[] audioData, String senderId);
    void onHeartAnimation(String username, boolean isLiked);
}

public Client(MessageListener listener) {
    this.messageListener = listener;
    this.executorService = Executors.newSingleThreadExecutor();
    this.isConnected = false;
}

public String getUsername() {
    return clientUsername;
}


/**
 * Broadcasts a discovery packet and waits for the server to respond.
 *
 * @param discoveryPort the UDP port on which the server is listening for discovery requests
 * @param timeoutMs how long (in ms) to wait for a response before giving up
 * @return the first responding server IP, or null if none found
 */

 
public static String findHost(int discoveryPort, int timeoutMs) {
    try (DatagramSocket socket = new DatagramSocket()) {
        socket.setBroadcast(true);
        // 1) Send discovery request
        byte[] requestData = "DISCOVER_MOVIEAPP_SERVER".getBytes(StandardCharsets.UTF_8);
        DatagramPacket request = new DatagramPacket(
            requestData, 
            requestData.length,
            InetAddress.getByName("255.255.255.255"), 
            discoveryPort
        );
        socket.send(request);

        // 2) Wait for the first reply
        socket.setSoTimeout(timeoutMs);
        byte[] buf = new byte[256];
        DatagramPacket response = new DatagramPacket(buf, buf.length);
        socket.receive(response);

        String msg = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
        if ("MOVIEAPP_SERVER_HERE".equals(msg)) {
            return response.getAddress().getHostAddress();
        }
    } catch (IOException e) {
        System.err.println("[Client][findHost] " + e.getMessage());
    }
    return null;
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
    
    audioSocket = new DatagramSocket();
    int localAudioPort = audioSocket.getLocalPort();
    sendMessage("AUDIO_PORT:" + localAudioPort);
    startAudioReceiveLoop();

    // Get username from UserSession
    String username = com.movieapp.model.UserSession.getInstance().getUsername();
    if (username != null) {
        clientUsername = username;
        sendMessage("SET_NAME:" + username);
    } else {
        // Fallback to asking for username if not logged in
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("User");
            dialog.setTitle("Enter your name");
            dialog.setHeaderText("Welcome to MovieApp Chat");
            dialog.setContentText("Please enter your username:");
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                clientUsername = name;
                sendMessage("SET_NAME:" + name);
            });
        });
    }

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

public void startAudioStreaming() {
    try {
        if (audioSocket == null || audioSocket.isClosed()) {
            audioSocket = new DatagramSocket();
            int localAudioPort = audioSocket.getLocalPort();
            sendMessage("AUDIO_PORT:" + localAudioPort);
            startAudioReceiveLoop();
        }

        if (audioExecutor == null || audioExecutor.isShutdown()) {
            audioExecutor = Executors.newSingleThreadExecutor();
        }

        audioExecutor.submit(() -> {
            AudioStreamUtils.startAudioStream(pcmBytes -> {
                try {
                    if (audioSocket != null && !audioSocket.isClosed()) {
                        AudioPacket pkt = new AudioPacket(pcmBytes, clientUsername);
                        byte[] buf = pkt.toBytes();
                        DatagramPacket dp = new DatagramPacket(
                            buf, buf.length,
                            InetAddress.getByName(socket.getInetAddress().getHostAddress()),
                            serverAudioPort
                        );
                        audioSocket.send(dp);
                    }
                } catch (IOException e) {
                    System.err.println("[Client] Error sending audio: " + e.getMessage());
                }
            });
        });
    } catch (IOException e) {
        System.err.println("[Client] Error starting audio streaming: " + e.getMessage());
    }
}


private void startAudioReceiveLoop() {
    if (audioExecutor == null || audioExecutor.isShutdown()) {
        audioExecutor = Executors.newSingleThreadExecutor();
    }

    audioExecutor.submit(() -> {
        byte[] buf = new byte[8192];
        
        while (isConnected && audioSocket != null && !audioSocket.isClosed()) {
            try {
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                audioSocket.receive(dp);
                
                if (audioSocket.isClosed()) break;
                
                AudioPacket packet = AudioPacket.fromBytes(dp.getData(), dp.getLength());
                if (packet != null && packet.getAudioData() != null) {
                    AudioStreamUtils.playAudioStream(packet.getAudioData());
                }
            } catch (IOException e) {
                if (!isConnected || audioSocket == null || audioSocket.isClosed()) break;
                System.err.println("[Client] Error receiving audio: " + e.getMessage());
            }
        }
    });
}


public void stopAudioStreaming() {
    AudioStreamUtils.stopAudioStream();
    isConnected = false;
    
    if (audioSocket != null) {
        try {
            audioSocket.close();
        } catch (Exception e) {
            System.err.println("[Client] Error closing audio socket: " + e.getMessage());
        }
        audioSocket = null;
    }
    
    if (audioExecutor != null) {
        audioExecutor.shutdownNow();
        audioExecutor = null;
    }
}

private void startListening() {
    executorService.submit(() -> {
        try {
            while (isConnected) {
                String message = in.readLine();
                if (message == null) break;
                
                if (message.startsWith("FILE:")) {
                    handleFileTransfer();
                } else if (message.startsWith("IMAGE_CHAT:")) {
                    handleImageChat(message);
                } else if (message.startsWith("AUDIO:")) {
                    handleAudioTransfer();
                } else if (message.startsWith("FRAME:")) {
                    handleFrameReceived(message);
                } else if (message.startsWith("HEART_ANIMATION:")) {
                    handleHeartAnimation(message);
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


private void handleFrameReceived(String message) {
    try {
        String base64 = message.substring(6); // Remove "FRAME:" prefix
        // Validate base64 string
        if (!base64.matches("^[A-Za-z0-9+/]*={0,2}$")) {
            System.err.println("Invalid base64 string received");
            return;
        }
        byte[] imageData = Base64.getDecoder().decode(base64);
        
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage bufferedImage = ImageIO.read(bis);
        
        if (bufferedImage != null) {
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            Platform.runLater(() -> {
                if (messageListener != null) {
                    messageListener.onImageReceived(fxImage, "screen");
                }
            });
        }
    } catch (IllegalArgumentException e) {
        System.err.println("Error decoding base64 frame: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Error processing received frame: " + e.getMessage());
        e.printStackTrace();
    }
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

private void handleImageChat(String message) {
    try {
        int index = message.indexOf('%', 12);
        String name = message.substring(12, index);
        String base64 = message.substring(index+1);

        byte[] data = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage img = ImageIO.read(bis);
        Image fxImage = SwingFXUtils.toFXImage(img, null);
        
        Platform.runLater(() -> {
            messageListener.onImageReceived(fxImage, name);
        });
    } catch (IOException e) {
        System.err.println("Error handling image chat: " + e.getMessage());
    }
}

private void handleAudioTransfer() {
    try {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        AudioPacket audioPacket = (AudioPacket) ois.readObject();
        messageListener.onAudioReceived(audioPacket.getAudioData(), audioPacket.getSenderId());
    } catch (Exception e) {
        System.err.println("[Client] Error receiving audio: " + e.getMessage());
    }
}

private void handleHeartAnimation(String message) {
    try {
        int index = message.indexOf('%', 15);
        String name = message.substring(15, index);
        String state = message.substring(index + 1);
        boolean isLiked = state.equals("1");
        
        Platform.runLater(() -> {
            messageListener.onHeartAnimation(name, isLiked);
        });
    } catch (Exception e) {
        System.err.println("Error handling heart animation: " + e.getMessage());
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
    stopAudioStreaming();
    AudioStreamUtils.stopPlayback();
    try {
        if (socket != null) socket.close();
        if (in != null) in.close();
        if (out != null) out.close();
    } catch (IOException e) {
        System.err.println("[Client] Error closing connection: " + e.getMessage());
    }
    messageListener.onConnectionClosed();
}

public void stop() {
    disconnect();
    if (executorService != null && !executorService.isShutdown()) {
        executorService.shutdownNow();
    }
    if (audioExecutor != null && !audioExecutor.isShutdown()) {
        audioExecutor.shutdownNow();
    }
}    
    
}
