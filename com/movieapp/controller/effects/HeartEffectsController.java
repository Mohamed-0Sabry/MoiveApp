package com.movieapp.controller.effects;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import com.movieapp.network.Client;

public class HeartEffectsController {
    private Button heartButton;
    private ImageView heartIcon;
    private Pane effectsPane;
    private boolean isLiked = false;
    private Client client;

    public HeartEffectsController(Button heartButton, ImageView heartIcon, Pane effectsPane, Client client) {
        this.heartButton = heartButton;
        this.heartIcon = heartIcon;
        this.effectsPane = effectsPane;
        this.client = client;
        
        if (heartButton != null) {
            heartButton.setOnAction(event -> {
                animateHeart();
                showHeartBurst();      // Local burst effect
                showFloatingHeart();   // Local floating heart effect
            });
        }
    }

    private void animateHeart() {
        // Simple grow and shrink animation
        Timeline animationTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(heartIcon.scaleXProperty(), 1),
                new KeyValue(heartIcon.scaleYProperty(), 1)
            ),
            new KeyFrame(Duration.millis(100),
                new KeyValue(heartIcon.scaleXProperty(), 1.4),
                new KeyValue(heartIcon.scaleYProperty(), 1.4)
            ),
            new KeyFrame(Duration.millis(200),
                new KeyValue(heartIcon.scaleXProperty(), 1),
                new KeyValue(heartIcon.scaleYProperty(), 1)
            )
        );
        
        isLiked = !isLiked;
        updateHeartStyles();
        animationTimeline.play();

        // Send message to other users
        if (client != null) {
            client.sendMessage("HEART_ANIMATION:" + (isLiked ? "1" : "0"));
        }
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

    public void showHeartBurst() {
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

    public void showFloatingHeart() {
        // Create a single heart that floats up
        ImageView floatingHeart = new ImageView(heartIcon.getImage());
        floatingHeart.setFitWidth(32);
        floatingHeart.setFitHeight(32);
        
        // Position at the center of the button
        double startX = heartButton.localToScene(heartButton.getWidth()/2, heartButton.getHeight()/2).getX();
        double startY = heartButton.localToScene(heartButton.getWidth()/2, heartButton.getHeight()/2).getY();
        double paneX = effectsPane.sceneToLocal(startX, startY).getX();
        double paneY = effectsPane.sceneToLocal(startX, startY).getY();
        
        floatingHeart.setLayoutX(paneX - 16);
        floatingHeart.setLayoutY(paneY - 16);
        effectsPane.getChildren().add(floatingHeart);
        
        // Animate the heart floating up
        Timeline floatTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(floatingHeart.opacityProperty(), 1),
                new KeyValue(floatingHeart.translateYProperty(), 0),
                new KeyValue(floatingHeart.scaleXProperty(), 1),
                new KeyValue(floatingHeart.scaleYProperty(), 1)
            ),
            // Zoom in quickly
            new KeyFrame(Duration.seconds(0.2),
                new KeyValue(floatingHeart.scaleXProperty(), 1.8),
                new KeyValue(floatingHeart.scaleYProperty(), 1.8)
            ),
            // Then float up, get even larger, and fade out
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(floatingHeart.opacityProperty(), 0),
                new KeyValue(floatingHeart.translateYProperty(), -100),
                new KeyValue(floatingHeart.scaleXProperty(), 2.5),
                new KeyValue(floatingHeart.scaleYProperty(), 2.5)
            )
        );
        
        floatTimeline.setOnFinished(e -> effectsPane.getChildren().remove(floatingHeart));
        floatTimeline.play();
    }
} 