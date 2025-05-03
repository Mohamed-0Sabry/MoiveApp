package comm.demo.controller;

import com.movieapp.Main;
import com.movieapp.model.StreamSession;
import com.movieapp.model.StreamSessionImpl;
import com.movieapp.model.ScreenShareSession;
import com.movieapp.model.CameraShareSession;
import com.movieapp.model.MediaStreamSession;
import com.movieapp.network.Server;
import com.movieapp.utils.ScreenCaptureUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Base64;
import java.io.File;
import java.io.ByteArrayInputStream;

public class HostController {
    @FXML private ImageView previewImage;
    @FXML private Label connectionStatus;

    private Server server;
    private StreamSessionImpl streamSession;
    private Stage chatStage;
    private ChatController chatController;

    public void initialize() {
        streamSession = new StreamSessionImpl();
        try {
            server = new Server();
            server.start(5555);
            updateConnectionStatus("Server started on port 5555");
        } catch (IOException e) {
            updateConnectionStatus("Error starting server: " + e.getMessage());
        }
    }

    @FXML
    private void onUploadFileClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.avi", "*.mkv", "*.mov")
        );

        File selectedFile = fileChooser.showOpenDialog(previewImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                server.sendFileToAll(selectedFile);
                streamSession.startMediaStream(selectedFile.getAbsolutePath());
                updateConnectionStatus("File sent to all viewers");
            } catch (Exception e) {
                updateConnectionStatus("Error sending file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onStartScreenShareClicked() {
        if (!streamSession.isScreenSharing()) {
            streamSession.startScreenShare();
            ScreenCaptureUtils.startScreenCapture(this::handleScreenFrame, 60);
            updateConnectionStatus("Screen sharing started");
        } else {
            streamSession.stopScreenShare();
            ScreenCaptureUtils.stopScreenCapture();
            updateConnectionStatus("Screen sharing stopped");
        }
    }

    @FXML
    private void onStartCameraShareClicked() {
        if (!streamSession.isCameraSharing()) {
            streamSession.startCameraShare();
            // TODO: Implement camera capture
            updateConnectionStatus("Camera sharing started");
        } else {
            streamSession.stopCameraShare();
            // TODO: Stop camera capture
            updateConnectionStatus("Camera sharing stopped");
        }
    }

    @FXML
    private void onPlayClicked() {
        if (streamSession.isStreaming()) {
            server.broadcastMessage("PLAY");
            updateConnectionStatus("Play command sent");
        }
    }

    @FXML
    private void onPauseClicked() {
        if (streamSession.isStreaming()) {
            server.broadcastMessage("PAUSE");
            updateConnectionStatus("Pause command sent");
        }
    }

    @FXML
    private void onStopClicked() {
        if (streamSession.isStreaming()) {
            streamSession.stopStreaming();
            server.broadcastMessage("STOP");
            updateConnectionStatus("Stream stopped");
        }
    }

    @FXML
    private void onOpenChatClicked() {
        if (chatStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/ChatWindow.fxml"));
                chatStage = new Stage();
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(Main.class.getResource("/com/movieapp/styles/chat.css").toExternalForm());

                chatStage.setScene(scene);
                chatController = loader.getController();
                chatController.setServer(server);
                chatStage.setTitle("Chat");
                chatStage.show();
            } catch (IOException e) {
                updateConnectionStatus("Error opening chat: " + e.getMessage());
            }
        } else {
            chatStage.show();
            chatStage.toFront();
        }
    }

    private void handleScreenFrame(byte[] frameData) {
        if (streamSession.isScreenSharing()) {
            Image image = new Image(new ByteArrayInputStream(frameData));
            previewImage.setImage(image);
            server.broadcastMessage("FRAME:" + Base64.getEncoder().encodeToString(frameData));
        }
    }

    private void updateConnectionStatus(String message) {
        connectionStatus.setText(message);
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        if (streamSession != null) {
            streamSession.stopStreaming();
        }
        if (chatStage != null) {
            chatStage.close();
        }
    }
} 