package com.movieapp.utils;

import javafx.stage.Stage;

public class StageManager {
    private static StageManager instance;
    
    private StageManager() {}
    
    public static StageManager getInstance() {
        if (instance == null) {
            instance = new StageManager();
        }
        return instance;
    }
    
    public void configureStage(Stage stage) {
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // Removes the "Press ESC to exit fullscreen" hint
    }
} 