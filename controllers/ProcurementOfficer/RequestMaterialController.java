package controllers.ProcurementOfficer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.ProcurementOfficer.*;
import model.ProjectManager.*;;

public class RequestMaterialController {

    @FXML private TableView<MaterialRequest> tblRequests;
    @FXML private TableColumn<MaterialRequest, String> colType;
    @FXML private TableColumn<MaterialRequest, String> colItem;
    @FXML private TableColumn<MaterialRequest, Integer> colQuantity;
    @FXML private TableColumn<MaterialRequest, String> colDate;
    @FXML private TableColumn<MaterialRequest, String> colActions;

    private MaterialRequestFacade facade = new MaterialRequestFacade(); // DAO for DB operations

    @FXML
    public void initialize() {

        colType.setCellValueFactory(data -> new SimpleStringProperty("Material"));
        colItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaterialName()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestedDate().toString()));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnApprove = new Button("Approve");
            private final Button btnReject = new Button("Reject");
            private final HBox container = new HBox(10, btnApprove, btnReject);

            {
                btnApprove.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-background-radius: 5;");
                btnReject.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 5;");

                btnApprove.setOnAction(event -> handleApprove(getTableView().getItems().get(getIndex())));
                btnReject.setOnAction(event -> handleReject(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        ObservableList<MaterialRequest> data = FXCollections.observableArrayList();
        String sql = """
            SELECT mr.id, mr.material_id, mr.project_id, mr.procurement_officer, mr.user_id, 
                   mr.quantity, mr.unit, mr.status, mr.requestDate, mr.processed, 
                   r.name AS material_name
            FROM material_requests mr
            LEFT JOIN Resources r ON r.id = mr.material_id
            WHERE mr.status = 'Pending'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                data.add(new MaterialRequest(
                        rs.getInt("id"),
                        rs.getInt("material_id"),
                        rs.getInt("project_id"),
                        rs.getInt("procurement_officer"),
                        rs.getInt("user_id"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getTimestamp("requestDate"),
                        rs.getInt("processed") == 1,
                        rs.getString("material_name")
                ));
            }
            tblRequests.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
            showPopup("Error", "Failed to load pending requests", Color.RED);
        }
    }

    private void handleApprove(MaterialRequest req) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Approve request for " + req.getMaterialName() + " (qty: " + req.getQuantity() + ")?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    facade.approveRequest(req.getId());

                    // Send notification
                    String msg = "Your request for " + req.getMaterialName() +
                            " (qty: " + req.getQuantity() +
                            ") has been APPROVED by Procurement Officer ID: " + req.getProcurementOfficerId() +
                            " for Project ID: " + req.getProjectId();
                    NotificationDAO.sendNotificationToUser(req.getUserId(), msg);

                    showPopup("Request Approved", req.getMaterialName(), Color.GREEN);
                    tblRequests.getItems().remove(req);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showPopup("Error", "Failed to approve request: " + ex.getMessage(), Color.RED);
                }
            }
        });
    }

    private void handleReject(MaterialRequest req) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Reject request for " + req.getMaterialName() + " (qty: " + req.getQuantity() + ")?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    facade.rejectRequest(req.getId());

                    // Send notification
                    String msg = "Your request for " + req.getMaterialName() +
                            " (qty: " + req.getQuantity() +
                            ") has been REJECTED by Procurement Officer ID: " + req.getProcurementOfficerId() +
                            " for Project ID: " + req.getProjectId();
                    NotificationDAO.sendNotificationToUser(req.getUserId(), msg);

                    showPopup("Request Rejected", req.getMaterialName(), Color.RED);
                    tblRequests.getItems().remove(req);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showPopup("Error", "Failed to reject request: " + ex.getMessage(), Color.RED);
                }
            }
        });
    }

    private void showPopup(String message, String item, Color color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(message);
        alert.setHeaderText(null);
        alert.setContentText(item + " - " + message);
        alert.getDialogPane().setStyle("-fx-background-color: #ffffff; -fx-font-size: 14px;");
        alert.showAndWait();
    }

    @FXML
    private void handleBack(MouseEvent event) {
        System.out.println("Back clicked");
        // Navigate back to previous screen
    }
}
