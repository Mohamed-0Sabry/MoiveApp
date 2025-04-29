package comm.demo.network;

import com.movieapp.model.FileTransfer;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executorService;
    private MessageListener messageListener;
    public boolean isConnected;

    public interface MessageListener {
        void onMessageReceived(String message);
        void onFileReceived(FileTransfer fileTransfer);
        void onConnectionClosed();
    }

    public Client(MessageListener listener) {
        this.messageListener = listener;
        this.executorService = Executors.newSingleThreadExecutor();
        this.isConnected = false;
    }

    public void connectToHost(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        isConnected = true;

        // Start listening for messages
        startListening();
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