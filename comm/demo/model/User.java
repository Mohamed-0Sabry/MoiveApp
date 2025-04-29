package comm.demo.model;

import java.net.Socket;

public class User {
    private String username;
    private boolean isHost;
    private Socket socket;

    public User(String username, boolean isHost, Socket socket) {
        this.username = username;
        this.isHost = isHost;
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public boolean isHost() {
        return isHost;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
