package com.movieapp.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.InputStreamReader;

public class TestingLoginPage extends Application {
    private static final String CONFIG_FILE = "com/movieapp/database/config.json";
    private static final String FXML_FILE = "com/movieapp/view/login_scene.fxml";
    private static final int MIN_WINDOW_WIDTH = 600;
    private static final int MIN_WINDOW_HEIGHT = 400;

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupStage(primaryStage);
        loadAndShowScene(primaryStage);
    }

    private void setupStage(Stage stage) throws Exception {
        JSONObject appSettings = loadAppSettings();
        
        stage.setTitle((String) appSettings.get("windowTitle"));
        stage.setWidth(((Long) appSettings.get("windowWidth")).doubleValue());
        stage.setHeight(((Long) appSettings.get("windowHeight")).doubleValue());
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setResizable(true);
    }

    private JSONObject loadAppSettings() throws Exception {
        JSONParser parser = new JSONParser();
        String configPath = System.getProperty("user.dir") + "/" + CONFIG_FILE;
        JSONObject config = (JSONObject) parser.parse(
            new java.io.FileReader(configPath)
        );
        return (JSONObject) config.get("appSettings");
    }

    private void loadAndShowScene(Stage stage) throws Exception {
        String fxmlPath = "file:" + System.getProperty("user.dir") + "/" + FXML_FILE;
        FXMLLoader loader = new FXMLLoader(new java.net.URL(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 