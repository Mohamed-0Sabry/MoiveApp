package com.movienight;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        MEDIA_COMMAND, 
        CHAT_MESSAGE,
        CLIENT_JOIN,
        CLIENT_LEAVE
    }
    
    private final Type type;
    private final Object payload;
    
    public NetworkMessage(Type type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
    
    public Type getType() {
        return type;
    }
    
    public Object getPayload() {
        return payload;
    }
}