package com.movieapp.controller.effects;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class HeartEffectsController {
    private Button heartButton;
    private ImageView heartIcon;
    private Pane effectsPane;
    private boolean isLiked = false;

    public HeartEffectsController(Button heartButton, ImageView heartIcon, Pane effectsPane) {
        this.heartButton = heartButton;
        this.heartIcon = heartIcon;
        this.effectsPane = effectsPane;
        
        if (heartButton != null) {
            heartButton.setOnAction(event -> {
                animateHeart();
                showHeartBurst();
            });
        }
    }

    private void animateHeart() {
        Timeline animationTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(heartIcon.scaleXProperty(), 1),
                new KeyValue(heartIcon.scaleYProperty(), 1),
                new KeyValue(heartIcon.rotateProperty(), 0),
                new KeyValue(heartIcon.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.millis(100),
                new KeyValue(heartIcon.scaleXProperty(), 1.4),
                new KeyValue(heartIcon.scaleYProperty(), 1.4),
                new KeyValue(heartIcon.rotateProperty(), 30),
                new KeyValue(heartIcon.opacityProperty(), 1.2)
            ),
            new KeyFrame(Duration.millis(200),
                new KeyValue(heartIcon.scaleXProperty(), 1),
                new KeyValue(heartIcon.scaleYProperty(), 1),
                new KeyValue(heartIcon.rotateProperty(), 0),
                new KeyValue(heartIcon.opacityProperty(), 1)
            )
        );
        
        isLiked = !isLiked;
        updateHeartStyles();
        animationTimeline.play();
    }

    private void updateHeartStyles() {
        if (isLiked) {
            heartButton.setStyle("-fx-background-color: transparent; " +
                               "-fx-border-radius: 50%; " +
                               "-fx-border: 2px solid #ff4d4d; " +
                               "-fx-cursor: hand; " +
                               "-fx-font-weight: bold; " +
                               "-fx-effect: dropshadow(gaussian, #ff9999, 10, 0.3, 0, 0);");
            heartIcon.setStyle("-fx-effect: dropshadow(gaussian, #ff9999, 12, 0.4, 0, 0);");
        } else {
            heartButton.setStyle("-fx-background-color: transparent; " +
                               "-fx-border-radius: 50%; " +
                               "-fx-border: 2px solid #cccccc; " +
                               "-fx-cursor: hand; " +
                               "-fx-font-weight: bold; " +
                               "-fx-effect: none;");
            heartIcon.setStyle("-fx-effect: none;");
        }
    }

    private void showHeartBurst() {
        for (int i = 0; i < 6; i++) {
            ImageView heart = new ImageView(heartIcon.getImage());
            heart.setFitWidth(24);
            heart.setFitHeight(24);
            
            double startX = heartButton.localToScene(heartButton.getWidth()/2, heartButton.getHeight()/2).getX();
            double startY = heartButton.localToScene(heartButton.getWidth()/2, heartButton.getHeight()/2).getY();
            double paneX = effectsPane.sceneToLocal(startX, startY).getX();
            double paneY = effectsPane.sceneToLocal(startX, startY).getY();
            
            heart.setLayoutX(paneX);
            heart.setLayoutY(paneY);
            effectsPane.getChildren().add(heart);
            
            double angle = Math.toRadians(60 * i + 20 - Math.random()*40);
            double distance = 80 + Math.random()*30;
            double dx = Math.cos(angle) * distance;
            double dy = Math.sin(angle) * distance;
            
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(heart.opacityProperty(), 1),
                    new KeyValue(heart.translateXProperty(), 0),
                    new KeyValue(heart.translateYProperty(), 0),
                    new KeyValue(heart.scaleXProperty(), 1),
                    new KeyValue(heart.scaleYProperty(), 1)
                ),
                new KeyFrame(Duration.seconds(0.7),
                    new KeyValue(heart.opacityProperty(), 0),
                    new KeyValue(heart.translateXProperty(), dx),
                    new KeyValue(heart.translateYProperty(), dy),
                    new KeyValue(heart.scaleXProperty(), 1.5),
                    new KeyValue(heart.scaleYProperty(), 1.5)
                )
            );
            
            tl.setOnFinished(e -> effectsPane.getChildren().remove(heart));
            tl.play();
        }
    }
} 