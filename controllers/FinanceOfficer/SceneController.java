package controllers.FinanceOfficer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class SceneController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void switchScene(ActionEvent event) {
        Node source = (Node) event.getSource();
        String fxmlFile = "";

        if (source.getId().equals("financeBtn")) {
            fxmlFile = "/screens/FinanceOfficer/finance_dashboard.fxml";
        } else if (source.getId().equals("otherBtn")) {
            fxmlFile = "/screens/FinanceOfficer/OtherPage.fxml"; // placeholder for future page
        }

        if (!fxmlFile.isEmpty()) {
            try {
                Node newContent = FXMLLoader.load(getClass().getResource(fxmlFile));
                contentArea.getChildren().setAll(newContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
