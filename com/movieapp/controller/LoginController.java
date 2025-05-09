package com.movieapp.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Interpolator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginController {
    private static final String USERS_FILE = "com/movieapp/database/users.json";
    private static final String ERROR_TITLE = "Error";
    private static final String SUCCESS_TITLE = "Success";
    private static final double FADE_DURATION = 0.4;
    
    @FXML private AnchorPane loginPanel;
    @FXML private VBox loginForm;
    @FXML private VBox signupForm;
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private TextField signupName;
    @FXML private TextField signupEmail;
    @FXML private PasswordField signupPassword;
    @FXML private PasswordField signupConfirmPassword;
    @FXML private ImageView illustrationImage;
    
    private JSONArray users;
    private boolean isTransitioning = false;

    @FXML
    public void initialize() {
        try {
            loadUsers();
            setupInitialStates();
        } catch (Exception e) {
            showError("Error initializing application: " + e.getMessage());
        }
    }

    private void setupInitialStates() {
        if (loginForm != null && signupForm != null) {
            loginForm.setOpacity(1.0);
            signupForm.setOpacity(0.0);
            signupForm.setVisible(false);
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        if (loginEmail == null || loginPassword == null) return;
        
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (authenticateUser(email, password)) {
            showSuccess("Login successful!");
            clearLoginFields();
        } else {
            showError("Invalid email or password");
        }
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        if (signupName == null || signupEmail == null || 
            signupPassword == null || signupConfirmPassword == null) return;
            
        String name = signupName.getText().trim();
        String email = signupEmail.getText().trim();
        String password = signupPassword.getText();
        String confirmPassword = signupConfirmPassword.getText();
        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        if (isEmailRegistered(email)) {
            showError("Email already registered");
            return;
        }
        
        registerNewUser(name, email, password);
        showSuccess("Account created successfully!");
        clearSignupFields();
        toggleForms(event);
    }

    @FXML
    public void toggleForms(ActionEvent event) {
        if (isTransitioning || loginForm == null || signupForm == null) return;
        
        isTransitioning = true;
        
        // Create parallel animations for both forms
        ParallelTransition transition = new ParallelTransition();
        
        // Fade out current form
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(FADE_DURATION), 
            loginForm.isVisible() ? loginForm : signupForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_OUT);
        fadeOut.setOnFinished(e -> {
            loginForm.setVisible(!loginForm.isVisible());
            signupForm.setVisible(!signupForm.isVisible());
        });
        
        // Fade in next form
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(FADE_DURATION), 
            loginForm.isVisible() ? signupForm : loginForm);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        
        // Add both animations to parallel transition
        transition.getChildren().addAll(fadeOut, fadeIn);
        
        // Reset transition flag when animation completes
        transition.setOnFinished(e -> isTransitioning = false);
        
        // Play the transition
        transition.play();
    }

    @FXML
    public void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #ffb6c1;");
    }
    
    @FXML
    public void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #fcd3e1;");
    }

    private void clearLoginFields() {
        if (loginEmail != null) loginEmail.clear();
        if (loginPassword != null) loginPassword.clear();
    }

    private void clearSignupFields() {
        if (signupName != null) signupName.clear();
        if (signupEmail != null) signupEmail.clear();
        if (signupPassword != null) signupPassword.clear();
        if (signupConfirmPassword != null) signupConfirmPassword.clear();
    }

    private void showError(String message) {
        showAlert(ERROR_TITLE, message, Alert.AlertType.ERROR);
    }

    private void showSuccess(String message) {
        showAlert(SUCCESS_TITLE, message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) {
                createInitialUsersFile();
            }
            users = loadUsersFromFile();
        } catch (IOException | ParseException e) {
            showError("Failed to load user data");
        }
    }

    private void createInitialUsersFile() throws IOException {
        JSONObject initialData = new JSONObject();
        initialData.put("users", new JSONArray());
        Files.write(Paths.get(USERS_FILE), initialData.toJSONString().getBytes());
    }

    private JSONArray loadUsersFromFile() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(new FileReader(USERS_FILE));
        return (JSONArray) data.get("users");
    }

    private void saveUsers() {
        try {
            JSONObject data = new JSONObject();
            data.put("users", users);
            Files.write(Paths.get(USERS_FILE), data.toJSONString().getBytes());
        } catch (IOException e) {
            showError("Failed to save user data");
        }
    }

    private boolean authenticateUser(String email, String password) {
        for (Object userObj : users) {
            JSONObject user = (JSONObject) userObj;
            if (user.get("email").equals(email) && user.get("password").equals(password)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmailRegistered(String email) {
        for (Object userObj : users) {
            JSONObject user = (JSONObject) userObj;
            if (user.get("email").equals(email)) {
                return true;
            }
        }
        return false;
    }

    private void registerNewUser(String name, String email, String password) {
        JSONObject newUser = new JSONObject();
        newUser.put("name", name);
        newUser.put("email", email);
        newUser.put("password", password);
        users.add(newUser);
        saveUsers();
    }
}