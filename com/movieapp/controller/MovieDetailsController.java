package com.movieapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import org.json.simple.JSONObject;

public class MovieDetailsController {
    @FXML private StackPane dialogPane;
    @FXML private ImageView posterImage;
    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private Label runtimeLabel;
    @FXML private Label ratingLabel;
    @FXML private Label genreLabel;
    @FXML private TextArea plotText;
    @FXML private Label directorLabel;
    @FXML private Label writersLabel;
    @FXML private Label actorsLabel;
    @FXML private Label awardsLabel;
    @FXML private Button closeButton;

    private JSONObject movieData;

    @FXML
    public void initialize() {
        
        dialogPane.setOpacity(0);
        dialogPane.setScaleX(0.9);
        dialogPane.setScaleY(0.9);

        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), dialogPane);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
        parallelTransition.play();

        closeButton.setOnAction(e -> closeDialog());
    }

    public void setMovieData(JSONObject movieData) {
        this.movieData = movieData;
        updateUI();
    }

    private void updateUI() {
        if (movieData != null) {
            
            String posterUrl = (String) movieData.get("Poster");
            if (posterUrl != null && !posterUrl.equals("N/A")) {
                Image image = new Image(posterUrl);
                posterImage.setImage(image);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), posterImage);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

            
            titleLabel.setText((String) movieData.get("Title"));
            yearLabel.setText((String) movieData.get("Year"));
            runtimeLabel.setText((String) movieData.get("Runtime"));
            ratingLabel.setText("IMDb Rating: " + movieData.get("imdbRating") + "/10");
            genreLabel.setText((String) movieData.get("Genre"));

            
            animateLabels(titleLabel, yearLabel, runtimeLabel, ratingLabel, genreLabel);

            
            plotText.setText((String) movieData.get("Plot"));
            directorLabel.setText((String) movieData.get("Director"));
            writersLabel.setText((String) movieData.get("Writer"));
            actorsLabel.setText((String) movieData.get("Actors"));
            awardsLabel.setText((String) movieData.get("Awards"));

            
            animateLabels(plotText, directorLabel, writersLabel, actorsLabel, awardsLabel);
        }
    }

    private void animateLabels(javafx.scene.Node... nodes) {
        SequentialTransition sequentialTransition = new SequentialTransition();
        
        for (javafx.scene.Node node : nodes) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), node);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            sequentialTransition.getChildren().add(fadeIn);
        }
        
        sequentialTransition.play();
    }

    private void closeDialog() {
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dialogPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), dialogPane);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);
        
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeOut, scaleOut);
        parallelTransition.setOnFinished(e -> {
            
            StackPane parent = (StackPane) dialogPane.getParent();
            parent.getChildren().remove(dialogPane);
        });
        parallelTransition.play();
    }
} 