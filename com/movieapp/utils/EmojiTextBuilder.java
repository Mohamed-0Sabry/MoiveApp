package com.movieapp.utils;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.util.TextUtils;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class EmojiTextBuilder {

    /**
     * Convert raw message text into a FlowPane of
     * JavaFX Text (for plain text) and ImageView (for emoji).
     */
    public static Node build(String message) {
        FlowPane pane = new FlowPane();
        pane.setHgap(1);
        pane.setVgap(1);

        try {
            for (Object part : TextUtils.convertToStringAndEmojiObjects(message)) {
                if (part instanceof String) {
                    pane.getChildren().add(new Text((String) part));
                } else if (part instanceof Emoji) {
                    Emoji e = (Emoji) part;
                    // Use text representation instead of image
                    Text emojiText = new Text(e.character());
                    emojiText.setStyle("-fx-font-size: 20px;");
                    pane.getChildren().add(emojiText);
                }
            }
        } catch (Exception e) {
            // If anything goes wrong, just return the text as is
            System.err.println("Error building emoji text: " + e.getMessage());
            pane.getChildren().add(new Text(message));
        }
        
        return pane;
    }
}   
