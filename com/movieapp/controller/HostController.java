import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;



public class HostController implements Initializable {
    @FXML
    private Button chatButton;
    @FXML
    private AnchorPane mainAP;
    public void initialize(URL location, ResourceBundle resources) {
        chatButton.setOnAction(event -> showChat());
    }
    private void showChat(){
        mainAP.setVisible(true);
    }
}
