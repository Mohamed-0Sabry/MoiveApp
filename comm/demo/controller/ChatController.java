package com.movieapp.controller;

import com.movieapp.network.Client;
import com.movieapp.network.Server;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController {
    @FXML private ListView<String> messageList;
    @FXML private TextField messageField;

    private Server server;
    private Client client;

    public void setServer(Server server) {
        this.server = server;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void onSendMessageClicked() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            if (server != null) {
                server.broadcastMessage("CHAT:" + message);
            } else if (client != null) {
                client.sendMessage("CHAT:" + message);
            }
            messageField.clear();
        }
    }

    public void addMessage(String message) {
        messageList.getItems().add(message);
        messageList.scrollTo(messageList.getItems().size() - 1);
    }
} 