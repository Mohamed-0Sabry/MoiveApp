package com.movieapp.utils;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class EmojiManager {

    private static final String[] SHORT_NAMES = {
        "smile", "laughing", "heart_eyes",
        "+1", "fire", "tada"
    };

    private final TextField target;
    private final Button[] buttons;

    public EmojiManager(TextField targetField, Button... buttons) {
        this.target = targetField;
        this.buttons = buttons;
    }

    public void initialize() {
        try {
            // disable until configured
            for (Button b : buttons) {
                b.setDisable(true);
            }
            setupButtons();
        } catch (Exception e) {
            System.err.println("Error initializing emoji buttons: " + e.getMessage());
            // Fallback to text-based emojis if image loading fails
            setupTextEmojis();
        }
    }

    private void setupButtons() {
        for (int i = 0; i < buttons.length && i < SHORT_NAMES.length; i++) {
            Button btn = buttons[i];
            String name = SHORT_NAMES[i];

            try {
                Emoji emoji = EmojiData.emojiFromShortName(name)
                    .orElseThrow(() -> new RuntimeException("Unknown emoji:" + name));

                // Create ImageView with proper error handling
                ImageView iv = new ImageView();
                iv.setFitHeight(24);
                iv.setPreserveRatio(true);
                
                // Set the image with error handling
                try {
                    // Get the emoji character and create a text-based button
                    String emojiChar = emoji.character();
                    btn.setText(emojiChar);
                    btn.setDisable(false);
                    btn.setOnAction(e -> target.appendText(emojiChar));
                } catch (Exception e) {
                    System.err.println("Error setting up emoji button " + name + ": " + e.getMessage());
                    // Fallback to text representation
                    setupTextEmoji(btn, i);
                }
            } catch (Exception e) {
                System.err.println("Error setting up emoji button " + name + ": " + e.getMessage());
                // Fallback to text representation
                setupTextEmoji(btn, i);
            }
        }
    }

    private void setupTextEmojis() {
        for (int i = 0; i < buttons.length && i < SHORT_NAMES.length; i++) {
            setupTextEmoji(buttons[i], i);
        }
    }

    private void setupTextEmoji(Button btn, int index) {
        // Fallback emoji characters
        String[] fallbackEmojis = {"😊", "😄", "😍", "👍", "🔥", "🎉"};
        System.out.println("Setting up text emoji for index: " + index);    
        if (index < fallbackEmojis.length) {
            btn.setText(fallbackEmojis[index]);
            btn.setDisable(false);
            btn.setOnAction(e -> target.appendText(fallbackEmojis[index]));
        }
    }
}
