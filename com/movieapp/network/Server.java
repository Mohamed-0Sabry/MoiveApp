package com.movieapp.network;

import com.movieapp.model.FileTransfer;
import com.movieapp.model.User;
import com.movieapp.model.AudioPacket;
import com.movieapp.utils.FileUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ServerSocket audioServerSocket;
    private List<User> connectedClients;
    private ExecutorService executorService;
    private boolean isRunning;
    private DatagramSocket audioSocket;
    private ExecutorService audioExecutor = Executors.newCachedThreadPool();

    public Server() {
        this.connectedClients = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.isRunning = false;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        audioServerSocket = new ServerSocket(5556);
        isRunning = true;

        new Thread(() -> {
            try (DatagramSocket udp = new DatagramSocket(8888)) {
                byte[] buf = new byte[256];
                while (isRunning) {
                    DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                    udp.receive(pkt);
                    String req = new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.UTF_8);
                    if ("DISCOVER_MOVIEAPP_SERVER".equals(req)) {
                        byte[] resp = "MOVIEAPP_SERVER_HERE".getBytes(StandardCharsets.UTF_8);
                        DatagramPacket reply = new DatagramPacket(
                                resp, resp.length,
                                pkt.getAddress(), pkt.getPort());
                        udp.send(reply);
                    }
                }
            } catch (IOException e) {
                System.err.println("[Server][Discovery] " + e.getMessage());
            }
        }, "discovery-responder").start();

        executorService.submit(this::handleAudioConnections);
        System.out.println("Server started on port " + port);
        audioSocket = new DatagramSocket(5556); // listen for client audio frames
        audioExecutor.submit(this::handleAudio);

        executorService.submit(() -> {
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    User newUser = new User("User" + connectedClients.size(), false, clientSocket);
                    connectedClients.add(newUser);
                    System.out.println("New client connected: " + newUser.getUsername());

                    handleClient(newUser);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void handleAudio() {
        byte[] buf = new byte[8192];
        System.out.println("[Server] Starting audio handling loop...");
        while (isRunning) {
            try {
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                audioSocket.receive(dp);
                System.out.println("[Server] Received audio packet from " + dp.getAddress() + ":" + dp.getPort());

                // deserialize
                AudioPacket pkt = AudioPacket.fromBytes(dp.getData(), dp.getLength());
                if (pkt != null && pkt.getAudioData() != null) {
                    System.out.println("[Server] Audio packet from: " + pkt.getSenderId() +
                            ", size: " + pkt.getAudioData().length + " bytes");
                    broadcastAudio(pkt);
                } else {
                    System.err.println("[Server] Failed to deserialize audio packet");
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("[Server] Error receiving audio: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleClient(User user) {

        System.out.println("NEW CLIENT: " + user.getUsername());

        executorService.submit(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(user.getSocket().getInputStream()));
                String message;

                while (isRunning && (message = in.readLine()) != null) {
                    System.out.println("Received from " + user.getUsername() + ": " + message);

                    handleMessage(message, user);
                }

            } catch (IOException e) {
                System.err.println("Error handling client " + user.getUsername() + ": " + e.getMessage());
            } finally {
                disconnectClient(user);
            }
        });
    }

    private void handleMessage(String message, User sender) {
        if (message.startsWith("CHAT:")) {
            String content = message.substring(5);
            broadcastMessage("CHAT:%" + sender.getUsername() + "%" + content, sender);
        }
        if (message.startsWith("IMAGE_CHAT:")) {
            String content = message.substring(11);
            broadcastMessage("IMAGE_CHAT:%" + sender.getUsername() + "%" + content, sender);
        }
        if (message.startsWith("INFO_CHAT:")) {
            broadcastMessage(message, sender);
        }
        if (message.startsWith("FRAME:")) {
            broadcastFrame(sender, message);
        }
        if (message.startsWith("SET_NAME:")) {
            String name = message.substring(9);
            sender.setUsername(name);
            System.out.println("Username set to " + name);
            broadcastMessage("INFO_CHAT:" + sender.getUsername() + " Joined To The Chat", sender);
        }
        if (message.startsWith("AUDIO:")) {
            handleAudioStream(sender);
        }
        if (message.startsWith("AUDIO_PORT:")) {
            try {
                int newPort = Integer.parseInt(message.substring(11));
                if (newPort != sender.getAudioPort()) {
                    System.out.println("[Server] Client " + sender.getUsername() + " changed audio port from " +
                            sender.getAudioPort() + " to " + newPort);
                    sender.setAudioPort(newPort);
                }
            } catch (NumberFormatException e) {
                System.err.println("[Server] Invalid audio port received from " + sender.getUsername());
            }
        }
        if (message.startsWith("HEART_ANIMATION:")) {
            String content = message.substring(15);
            broadcastMessage("HEART_ANIMATION:%" + sender.getUsername() + "%" + content, null);
        }
    }

    private void handleAudioStream(User sender) {
        try {
            ObjectInputStream ois = new ObjectInputStream(sender.getSocket().getInputStream());
            AudioPacket audioPacket = (AudioPacket) ois.readObject();
            broadcastAudio(audioPacket);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling audio stream from " + sender.getUsername() + ": " + e.getMessage());
        }
    }

    private void broadcastAudio(AudioPacket packet) {
        if (packet == null) {
            System.err.println("[Server] Cannot broadcast null audio packet");
            return;
        }

        byte[] outBuf = packet.toBytes();
        if (outBuf == null) {
            System.err.println("[Server] Error serializing audio packet");
            return;
        }
        System.out.println("[Server] Broadcasting audio from " + packet.getSenderId() + " to " + connectedClients.size()
                + " clients");

        int successCount = 0;
        for (User u : connectedClients) {
            if (u.getAudioPort() <= 0) {
                System.err.println("[Server] Skipping client " + u.getUsername() + " - no audio port registered");
                continue;
            }

            InetAddress addr = u.getSocket().getInetAddress();
            int port = u.getAudioPort();
            try {
                DatagramPacket dp = new DatagramPacket(outBuf, outBuf.length, addr, port);
                audioSocket.send(dp);
                successCount++;
                System.out.println("[Server] Sent audio to " + u.getUsername() + " at " + addr + ":" + port);
            } catch (IOException ex) {
                System.err.println("[Server] Error sending audio to " + u.getUsername() + ": " + ex.getMessage());
            }
        }
        System.out.println("[Server] Successfully sent audio to " + successCount + " out of " + connectedClients.size()
                + " clients");
    }

    public void broadcastMessage(String message, User sender) {
        for (User client : connectedClients) {
            if (!client.equals(sender)) {
                try {
                    PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    System.err.println("Error broadcasting to " + client.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    private void broadcastFrame(User sender, String frameMessage) {
        for (User client : connectedClients) {
            if (!client.equals(sender)) {
                try {
                    PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                    out.println(frameMessage);
                } catch (IOException e) {
                    System.err.println("Error sending frame to " + client.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    public void sendFileToAll(File file) {
        try {
            byte[] fileData = FileUtils.readFileToBytes(file.getAbsolutePath());
            FileTransfer fileTransfer = new FileTransfer(file.getName(), file.length(), fileData);

            for (User client : connectedClients) {
                try {
                    ObjectOutputStream out = new ObjectOutputStream(client.getSocket().getOutputStream());
                    out.writeObject(fileTransfer);
                } catch (IOException e) {
                    System.err.println("Error sending file to " + client.getUsername() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void disconnectClient(User user) {
        connectedClients.remove(user);
        try {
            user.getSocket().close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        System.out.println("Client disconnected: " + user.getUsername());
        broadcastMessage("INFO_CHAT:" + user.getUsername() + " Leaved", user);
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (audioServerSocket != null) {
                audioServerSocket.close();
            }
            if (audioSocket != null) {
                audioSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server sockets: " + e.getMessage());
        }
        executorService.shutdown();
        audioExecutor.shutdown();
    }

    private void handleAudioConnections() {
        while (isRunning) {
            try {
                Socket audioClientSocket = audioServerSocket.accept();
                executorService.submit(() -> {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(audioClientSocket.getInputStream());
                        AudioPacket packet = (AudioPacket) ois.readObject();
                        broadcastAudio(packet);
                        ois.close();
                        audioClientSocket.close();
                    } catch (Exception e) {
                        System.err.println("Audio Error: " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.err.println("Audio socket error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5555;
        Server server = new Server();
        server.start(port);

    }
}