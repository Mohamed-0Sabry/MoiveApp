package com.movieapp.network;

import com.movieapp.model.FileTransfer;
import com.movieapp.model.User;
import com.movieapp.utils.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private List<User> connectedClients;
    private ExecutorService executorService;
    private boolean isRunning;

    public Server() {
        this.connectedClients = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.isRunning = false;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        System.out.println("Server started on port " + port);

        executorService.submit(() -> {
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    User newUser = new User("User" + connectedClients.size(), false, clientSocket);
                    connectedClients.add(newUser);
                    System.out.println("New client connected: " + newUser.getUsername());
                    
                    // Start a new thread to handle this client
                    handleClient(newUser);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void handleClient(User user) {
        executorService.submit(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(user.getSocket().getInputStream()));
                String message;
                
                while (isRunning && (message = in.readLine()) != null) {
                    System.out.println("Received from " + user.getUsername() + ": " + message);
                    // Handle different types of messages
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
            broadcastMessage("CHAT:" + sender.getUsername() + ": " + message.substring(5));
        }
        if (message.startsWith("FRAME:")) {
            broadcastFrame(sender, message);
        }        
    }

    public void broadcastMessage(String message) {
        for (User client : connectedClients) {
            try {
                PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                System.err.println("Error broadcasting to " + client.getUsername() + ": " + e.getMessage());
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
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        executorService.shutdown();
    }
} 