package controllers.ProcurementOfficer;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import model.ProcurementOfficer.MaterialRequest;
import model.ProcurementOfficer.MaterialRequestDAO;
import model.ProcurementOfficer.MaterialRequestFacade;
import model.ProjectManager.App;
import model.ProjectManager.NotificationDAO;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private BorderPane rootPane;

    // Sidebar buttons
    @FXML private Button btnSidebarDashboard;
    @FXML private Button btnSidebarReview;
    @FXML private Button btnSidebarAlerts;

    // Summary cards
    @FXML private Label lblPending;
    @FXML private Label lblApproved;
    @FXML private Label lblRejected;
    @FXML private Label lblTotal;
     @FXML private MenuItem logoutMenuItem;

    // Quick actions
    @FXML private Button btnReviewRequests;

    // Table
    @FXML private TableView<MaterialRequest> tblPending;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, String> colType;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, String> colItem;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, String> colProject;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, String> colRequester;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, Integer> colQuantity;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, String> colDate;
    @FXML private javafx.scene.control.TableColumn<MaterialRequest, Void> colActions;

    // Recent activity
    @FXML private VBox recentActivityContainer;

    private final MaterialRequestDAO dao = new MaterialRequestDAO();
    private final MaterialRequestFacade facade = new MaterialRequestFacade();

    @FXML
    public void initialize() {
        try {
            refreshAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sidebar navigation
        btnSidebarDashboard.setOnMouseClicked(this::onDashboardClick);
        btnSidebarReview.setOnMouseClicked(this::onReviewClick);
        btnSidebarAlerts.setOnMouseClicked(this::onAlertsClick);
        btnReviewRequests.setOnMouseClicked(this::onReviewClick);
        logoutMenuItem.setOnAction(e -> logout());
    }

    private void refreshAll() throws SQLException {
        // load pending requests into table
        List<MaterialRequest> pending = dao.getPendingRequests();
        ObservableList<MaterialRequest> data = FXCollections.observableArrayList(pending);
        tblPending.setItems(data);

        // configure table columns (once)
        configureTableColumns();

        // Update summary cards
        updateSummaryCards();

        // Update recent activity (last few requests)
        updateRecentActivity();
    }

    private void configureTableColumns() {
        // Only configure cell value factories once if not already done
        if (colItem.getCellValueFactory() == null) {
            colType.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty("Material")); // you can map type if present
            colItem.setCellValueFactory(cell -> cell.getValue().materialNameProperty());
            colProject.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getProjectId())));
            colRequester.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getProcurementOfficerId())));
            colQuantity.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());
            colDate.setCellValueFactory(cell -> {
                java.sql.Timestamp t = cell.getValue().getRequestedDate();
                String s = t == null ? "" : t.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(s);
            });

            // actions column (approve/reject)
            colActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnApprove = new Button("Approve");
                private final Button btnReject = new Button("Reject");
                private final HBox pane = new HBox(8, btnApprove, btnReject);

                {
                    btnApprove.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    btnReject.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                    btnApprove.setOnAction(e -> {
                        MaterialRequest req = getTableView().getItems().get(getIndex());
                        handleApprove(req);
                    });

                    btnReject.setOnAction(e -> {
                        MaterialRequest req = getTableView().getItems().get(getIndex());
                        handleReject(req);
                    });
                }

                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    setGraphic(empty ? null : pane);
                }
            });
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

            int userId = req.getUserId();
            int pID=req.getProjectId();
            int poId = req.getProcurementOfficerId(); 
            String msg = "Your request for " + req.getMaterialName() + 
                        " (qty: " + req.getQuantity() + 
                        ") has been APPROVED by Procurement Officer ID: " + poId + " for Project ID :" + pID + ".";
            NotificationDAO.sendNotificationToUser(userId, msg);

                showAnimatedPopup(req.getMaterialName(), true);
                refreshAfterAction();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Failed to approve request: " + ex.getMessage());
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

                // ⭐ SEND NOTIFICATION TO REQUESTER
                 int userId = req.getUserId();
            int pID=req.getProjectId();
            int poId = req.getProcurementOfficerId(); 
            String msg = "Your request for " + req.getMaterialName() + 
                        " (qty: " + req.getQuantity() + 
                        ") has been REJECTED by Procurement Officer ID: " + poId + " for Project ID :" + pID + ".";
            NotificationDAO.sendNotificationToUser(userId, msg);

                showAnimatedPopup(req.getMaterialName(), false);
                refreshAfterAction();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Failed to reject request: " + ex.getMessage());
            }
        }
    });
}

    private void refreshAfterAction() {
        try {
            refreshAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSummaryCards() throws SQLException {
        int pending = dao.countByStatus("Pending");
        int approved7 = dao.countByStatusLastDays("Approved", 7);
        int rejected7 = dao.countByStatusLastDays("Rejected", 7);
        int last7 = dao.countByStatusLastDays("Pending", 7) + approved7 + rejected7;

        lblPending.setText(String.valueOf(pending));
        lblApproved.setText(String.valueOf(approved7));
        lblRejected.setText(String.valueOf(rejected7));
        lblTotal.setText(String.valueOf(last7));
    }

    private void updateRecentActivity() throws SQLException {
        recentActivityContainer.getChildren().clear();
        // show last 5 requests (any status)
        List<MaterialRequest> all = dao.getAllRequests();
        int limit = Math.min(5, all.size());
        for (int i = 0; i < limit; i++) {
            MaterialRequest m = all.get(i);
            Label l = new Label(String.format("%s — %s (qty %d) — %s", m.getRequestedDate(), m.getMaterialName(), m.getQuantity(), m.getStatus()));
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            recentActivityContainer.getChildren().add(l);
        }
    }

    private void showAnimatedPopup(String itemName, boolean approved) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(approved ? "Approved" : "Rejected");

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 18; -fx-alignment: center; -fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8,0,0,4);");
        Label lbl = new Label((approved ? "Approved ✅ " : "Rejected ❌ ") + itemName);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (approved ? "#2e7d32;" : "#c62828;"));
        Button ok = new Button("OK");
        ok.setOnAction(e -> dialog.close());
        ok.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        content.getChildren().addAll(lbl, ok);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        FadeTransition ft = new FadeTransition(Duration.millis(280), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        dialog.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    // Navigation handlers (load center content similar to inventory)
    private void onDashboardClick(MouseEvent e) {
        loadCenterContent("/screens/ProcurementOfficer/Dashboard.fxml");
    }

    private void onReviewClick(MouseEvent e) {
        loadCenterContent("/screens/ProcurementOfficer/RequestMaterial.fxml");
    }

    private void onAlertsClick(MouseEvent e) {
      
         loadCenterContent("/screens/ProcurementOfficer/notifications.fxml");
    }

    private void loadCenterContent(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent loaded = loader.load();

            if (loaded instanceof BorderPane bp) {
                Node newTop = bp.getTop();
                Node newCenter = bp.getCenter();
                if (newCenter != null) rootPane.setCenter(newCenter);
                if (newTop != null) rootPane.setTop(newTop);
            } else {
                rootPane.setCenter(loaded);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

   @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            App.changeScene("Login", scene, "/screens/ProjectManager/login.css");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
