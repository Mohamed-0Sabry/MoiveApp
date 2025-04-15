package com.movienight;

import com.movienight.MediaCommand;
import com.movienight.NetworkMessage;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NetworkManager {
    // Constants
    private static final int DISCOVERY_PORT = 8888;
    private static final int COMMAND_PORT = 8889;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int BUFFER_SIZE = 4096;
    
    // For testing on the same machine
    private static boolean isTestMode = false;
    private static int testDiscoveryPort = DISCOVERY_PORT;
    private static int testCommandPort = COMMAND_PORT;
    private static boolean isTestHost = false;
    private static boolean isTestViewer = false;
    
    // Network properties
    private DatagramSocket discoverySocket;
    private ServerSocket commandServer;
    private ExecutorService executorService;
    private boolean running = false;
    
    // Observable properties for UI binding
    private final BooleanProperty isHost = new SimpleBooleanProperty(false);
    private final BooleanProperty hostExists = new SimpleBooleanProperty(false);
    private final StringProperty hostAddress = new SimpleStringProperty("");
    private final ObservableList<Socket> connectedClients = FXCollections.observableArrayList();
    
    // Callback for media commands
    private Consumer<MediaCommand> mediaCommandHandler;
    
    public NetworkManager() {
        executorService = Executors.newCachedThreadPool();
        startNetworkDiscovery();
    }
    
    // Method to enable test mode with different ports
    public static void enableTestMode(int discoveryPort, int commandPort) {
        isTestMode = true;
        testDiscoveryPort = discoveryPort;
        testCommandPort = commandPort;
    }
    
    // Method to set test role (host or viewer)
    public static void setTestRole(boolean isHost) {
        isTestMode = true;
        isTestHost = isHost;
        isTestViewer = !isHost;
        
        // Use appropriate ports based on role
        if (isHost) {
            // Host uses default ports
            testDiscoveryPort = DISCOVERY_PORT;
            testCommandPort = COMMAND_PORT;
        } else {
            // Viewer uses different ports
            testDiscoveryPort = 8890;
            testCommandPort = 8891;
        }
    }
    
    private void startNetworkDiscovery() {
        try {
            // Use test ports if in test mode
            int port = isTestMode ? testDiscoveryPort : DISCOVERY_PORT;
            
            discoverySocket = new DatagramSocket(port);
            discoverySocket.setBroadcast(true);
            running = true;
            
            // Start listening for host announcements
            executorService.submit(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                while (running) {
                    try {
                        discoverySocket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        
                        // Process the message
                        if (message.startsWith("HOST_ANNOUNCE:")) {
                            String hostIp = message.substring("HOST_ANNOUNCE:".length());
                            Platform.runLater(() -> {
                                hostExists.set(true);
                                hostAddress.set(hostIp);
                            });
                        }
                    } catch (IOException e) {
                        if (!discoverySocket.isClosed()) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    
    public boolean checkIfHostExists() {
        try {
            String message = "HOST_DISCOVERY_REQUEST";
            byte[] buffer = message.getBytes();
            
            // In test mode, try both the test discovery port and the default discovery port
            if (isTestMode) {
                // First try the test discovery port
                try {
                    DatagramPacket packet = new DatagramPacket(
                        buffer, buffer.length,
                        InetAddress.getByName(BROADCAST_ADDRESS),
                        testDiscoveryPort
                    );
                    
                    discoverySocket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Then try the default discovery port
                try {
                    DatagramPacket packet = new DatagramPacket(
                        buffer, buffer.length,
                        InetAddress.getByName(BROADCAST_ADDRESS),
                        DISCOVERY_PORT
                    );
                    
                    discoverySocket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Normal mode - just use the default discovery port
                DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length,
                    InetAddress.getByName(BROADCAST_ADDRESS),
                    DISCOVERY_PORT
                );
                
                discoverySocket.send(packet);
            }
            
            // Wait a bit for responses
            Thread.sleep(1000);
            
            return hostExists.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void startHosting() {
        if (running) return;
        
        running = true;
        isHost.set(true);
        
        // Use test ports if in test mode
        int port = isTestMode ? testCommandPort : COMMAND_PORT;
        
        try {
            commandServer = new ServerSocket(port);
            executorService.submit(this::startCommandServer);
            
            // Announce presence as host
            executorService.submit(() -> {
                try {
                    Thread.sleep(500);
                    while (running && isHost.get()) {
                        String myIp = getLocalIpAddress();
                        System.out.println("Announcing host at " + myIp + ":" + port);
                        String message = "HOST_ANNOUNCE:" + myIp;
                        byte[] buffer = message.getBytes();
                        

                        if (isTestMode) {
                            announceOnBothPorts(buffer);
                        } else {
                            announceOnDefaultPort(buffer);
                        }
                        

                        // In test mode, announce on both the default discovery port and the test discovery port
                        if (isTestMode) {
                            // Announce on the default discovery port
                            DatagramPacket packet = new DatagramPacket(
                                buffer, buffer.length,
                                InetAddress.getByName(BROADCAST_ADDRESS),
                                DISCOVERY_PORT
                            );
                            
                            discoverySocket.send(packet);
                            
                            // Also announce on the test discovery port
                            packet = new DatagramPacket(
                                buffer, buffer.length,
                                InetAddress.getByName(BROADCAST_ADDRESS),
                                testDiscoveryPort
                            );
                            
                            discoverySocket.send(packet);
                        } else {
                            // Normal mode - just use the default discovery port
                            DatagramPacket packet = new DatagramPacket(
                                buffer, buffer.length,
                                InetAddress.getByName(BROADCAST_ADDRESS),
                                DISCOVERY_PORT
                            );
                            
                            discoverySocket.send(packet);
                        }
                        
                    Thread.sleep(1000); // Announce every 1 second for quicker discovery
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
            isHost.set(false);
        }
    }
    

    private void announceOnBothPorts(byte[] buffer) throws IOException {
        // Announce on the default discovery port
        DatagramPacket packet = new DatagramPacket(
            buffer, buffer.length,
            InetAddress.getByName(BROADCAST_ADDRESS),
            DISCOVERY_PORT
        );
        discoverySocket.send(packet);
        
        // Also announce on the test discovery port
        packet = new DatagramPacket(
            buffer, buffer.length,
            InetAddress.getByName(BROADCAST_ADDRESS),
            testDiscoveryPort
        );
        discoverySocket.send(packet);
        
        // Additional: direct localhost packets for same-machine testing
        packet = new DatagramPacket(
            buffer, buffer.length,
            InetAddress.getByName("127.0.0.1"),
            testDiscoveryPort
        );
        discoverySocket.send(packet);
    }

    private void announceOnDefaultPort(byte[] buffer) throws IOException {
        DatagramPacket packet = new DatagramPacket(
            buffer, buffer.length,
            InetAddress.getByName(BROADCAST_ADDRESS),
            DISCOVERY_PORT
        );
        discoverySocket.send(packet);
    }
    
    private void startCommandServer() {
        try {
            while (running && isHost.get()) {
                Socket clientSocket = commandServer.accept();
                Platform.runLater(() -> connectedClients.add(clientSocket));
                
                // Handle this client in a separate thread
                executorService.submit(() -> handleClientConnection(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleClientConnection(Socket clientSocket) {
        try (
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            while (running && isHost.get() && !clientSocket.isClosed()) {
                // Read incoming messages from viewers
                Object obj = inputStream.readObject();
                if (obj instanceof NetworkMessage) {
                    processNetworkMessage((NetworkMessage) obj);
                }
            }
        } catch (Exception e) {
            // Handle client disconnect
            Platform.runLater(() -> connectedClients.remove(clientSocket));
        }
    }
    
    public void connectToHost() {
        if (running) return;
        
        running = true;
        isHost.set(false);
        
        // Use test ports if in test mode
        int port = isTestMode ? testCommandPort : COMMAND_PORT;
        
        if (hostAddress.get().isEmpty()) {
            return;
        }
        
        executorService.submit(() -> {
            try {
                Socket socket = new Socket(hostAddress.get(), port);
                
                // Set up streams
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                
                // Listen for commands from host
                while (running && !socket.isClosed()) {
                    Object obj = inputStream.readObject();
                    if (obj instanceof NetworkMessage) {
                        NetworkMessage message = (NetworkMessage) obj;
                        
                        if (message.getType() == NetworkMessage.Type.MEDIA_COMMAND) {
                            MediaCommand command = (MediaCommand) message.getPayload();
                            
                            // Execute on JavaFX thread
                            if (mediaCommandHandler != null) {
                                Platform.runLater(() -> mediaCommandHandler.accept(command));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void processNetworkMessage(NetworkMessage message) {
        // Process different message types
    }
    
    public void broadcastMediaCommand(MediaCommand command) {
        if (!isHost.get() || connectedClients.isEmpty()) {
            return;
        }
        
        NetworkMessage message = new NetworkMessage(NetworkMessage.Type.MEDIA_COMMAND, command);
        
        for (Socket clientSocket : connectedClients) {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void setMediaCommandHandler(Consumer<MediaCommand> handler) {
        this.mediaCommandHandler = handler;
    }
    
    public String getLocalIpAddress() {
        return "127.0.0.1";
        /*
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
        */
    }
    
    public void shutdown() {
        running = false;
        isHost.set(false);
        
        if (discoverySocket != null && !discoverySocket.isClosed()) {
            discoverySocket.close();
        }
        
        if (commandServer != null && !commandServer.isClosed()) {
            try {
                commandServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Close all client connections
        for (Socket socket : connectedClients) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        executorService.shutdownNow();
    }
    
    public BooleanProperty hostExistsProperty() {
        return hostExists;
    }
    
    public StringProperty hostAddressProperty() {
        return hostAddress;
    }
    
    public ObservableList<Socket> getConnectedClients() {
        return connectedClients;
    }
}