package com.movienight;

import java.io.Serializable;

public class MediaCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        LOAD,
        PLAY,
        PAUSE,
        STOP,
        SEEK
    }
    
    private final Type type;
    private final Object data;
    
    public MediaCommand(Type type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    public Type getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
}