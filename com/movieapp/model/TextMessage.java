package com.movieapp.model;

import java.io.Serializable;

public class TextMessage implements Serializable {
    private String type;  // "CHAT", "INFO", "SET_NAME"
    private String content;

    public TextMessage(String type, String content) {
        this.type = type;
        this.content = content;
    }

    // Getters and setters
    public String getType() { return type; }
    public String getContent() { return content; }
}

