package controllers.ProjectManager;

import model.ProjectManager.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;


public class DashboardController {

    @FXML private ListView<String> menuList;

    @FXML private VBox cardTotal;
    @FXML private VBox cardActive;
    @FXML private VBox cardPending;
    @FXML private VBox cardBudget;
    @FXML private VBox projectsBox;

    @FXML private BarChart<String, Number> progressChart;
    @FXML private PieChart budgetPie;
    @FXML private StackPane mainContent;
    @FXML private Label lblUsername;

    @FXML private VBox milestonesBox;
    @FXML private Button btnNotifications;
    @FXML private Button btnQuickLogout;

    // LEFT SIDEBAR HBOXES
    @FXML private HBox hbCreateProject;
    @FXML private HBox hbAssignResources;
    @FXML private HBox hbRequestMaterial;
    @FXML private HBox hbAssignWorkers;
    @FXML private HBox hbProjectPhase;
    @FXML private HBox hbManageInventory;
    @FXML private HBox hbDailyLog;
    @FXML private HBox hbDocuments;
    @FXML private HBox hbNotifications;
    @FXML private HBox hbBudget;


    // ICONS
    @FXML private ImageView iconAdd;
    @FXML private ImageView iconResources;
    @FXML private ImageView iconWorkers;
    @FXML private ImageView iconMaterial;
    @FXML private ImageView iconpp;
    @FXML private ImageView iconmi;
    @FXML private ImageView icondl;
    @FXML private ImageView icond;
    @FXML private ImageView iconn;
    @FXML private ImageView iconb;

    @FXML
    public void initialize() {
        // Load icons
        System.out.println(getClass().getResource("/icons/add.png"));

        if (iconAdd != null) iconAdd.setImage(new Image(getClass().getResourceAsStream("/icons/add.png")));
        if (iconResources != null) iconResources.setImage(new Image(getClass().getResourceAsStream("/icons/res.png")));
        if (iconWorkers != null) iconWorkers.setImage(new Image(getClass().getResourceAsStream("/icons/w.png")));
        if (iconMaterial != null) iconMaterial.setImage(new Image(getClass().getResourceAsStream("/icons/req.png")));
        if (iconpp != null) iconpp.setImage(new Image(getClass().getResourceAsStream("/icons/phase.png")));
        if (iconmi != null) iconmi.setImage(new Image(getClass().getResourceAsStream("/icons/ii.png")));
        if (icondl != null) icondl.setImage(new Image(getClass().getResourceAsStream("/icons/log.png")));
        if (icond != null) icond.setImage(new Image(getClass().getResourceAsStream("/icons/doc.png")));
        if (iconn != null) iconn.setImage(new Image(getClass().getResourceAsStream("/icons/noti.png")));
        if (iconb != null) iconb.setImage(new Image(getClass().getResourceAsStream("/icons/bud.png")));
        // Hover animations for sidebar HBoxes
        setupHoverAnimation(hbCreateProject);
        setupHoverAnimation(hbAssignResources);
        setupHoverAnimation(hbAssignWorkers);
        setupHoverAnimation(hbRequestMaterial);
        setupHoverAnimation(hbProjectPhase);
        setupHoverAnimation(hbManageInventory);
        setupHoverAnimation(hbDailyLog);
        setupHoverAnimation(hbDocuments);
         setupHoverAnimation(hbNotifications);
        setupHoverAnimation(hbBudget);


        if (menuList != null) menuList.getSelectionModel().select(0);

        loadChartWithAnimation();
        loadMilestones();
        loadDashboardData();
        setUsername(App.getCurrentUser().getUsername());
    }

    private void setupHoverAnimation(HBox box) {
        if (box == null) return;

        String normalStyle = "-fx-padding:10; -fx-background-radius:8;";
        String hoverStyle = "-fx-background-color:#34495E; -fx-padding:10; -fx-background-radius:8;";

        box.setStyle(normalStyle);

        box.setOnMouseEntered(e -> box.setStyle(hoverStyle));
        box.setOnMouseExited(e -> box.setStyle(normalStyle));
    }
   private void loadChartWithAnimation() {
    List<Project> projects = ProjectDAO.getAllProjects();
    System.out.println("Projects loaded: " + projects.size());

    if (progressChart != null) {
        progressChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Project Phase Progress");

        for (Project p : projects) {
            List<ProjectPhase> phases = ProjectPhaseDAO.getPhasesByProject(p.getId());
            if (phases.isEmpty()) continue;

            double completion = 0;
            String label = "Not started";

            // Count completed phases
            int completedPhases = 0;
            ProjectPhase currentPhase = null;
            for (ProjectPhase ph : phases) {
                if ("Completed".equalsIgnoreCase(ph.getStatus())) {
                    completedPhases++;
                } else if ("In Progress".equalsIgnoreCase(ph.getStatus())) {
                    currentPhase = ph;
                    break; // first in-progress phase
                }
            }

            if (currentPhase != null) {
                // calculate % of current phase based on dates
                java.util.Date now = new java.util.Date();
                long totalMillis = currentPhase.getEndDate().getTime() - currentPhase.getStartDate().getTime();
                long elapsedMillis = now.getTime() - currentPhase.getStartDate().getTime();
                double phaseProgress = totalMillis > 0 ? Math.min((double) elapsedMillis / totalMillis, 1.0) : 0;

                // total progress including completed phases
                completion = ((completedPhases + phaseProgress) / phases.size()) * 100;
                label = currentPhase.getName();
            } else {
                // all completed or not started
                if (completedPhases == phases.size()) {
                    completion = 100;
                    label = phases.get(phases.size() - 1).getName();
                } else {
                    completion = 0;
                }
            }

            XYChart.Data<String, Number> data = new XYChart.Data<>(p.getName() + "\n(" + label + ")", 0);
            series.getData().add(data);

            // animate bar
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(1.2),
                    new javafx.animation.KeyValue(data.YValueProperty(), completion)
            ));
            timeline.setDelay(Duration.millis(200 * projects.indexOf(p))); // stagger animation
            timeline.play();
        }

        progressChart.getData().add(series);
        progressChart.layout();
    } else {
        System.out.println("progressChart is null!");
    }
}




private void loadMilestones() {
    projectsBox.getChildren().clear();

    // Header
    Label header = new Label("Projects & Milestones");
    header.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
    projectsBox.getChildren().add(header);

    // Fetch projects from database
    List<Project> projects = ProjectDAO.getAllProjects();

    for (Project p : projects) {
        VBox projectCard = new VBox(10);
        projectCard.setStyle("-fx-background-color:white; -fx-padding:15; -fx-border-radius:10; -fx-background-radius:10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Header row: project name, status, delete button
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label projectName = new Label(p.getName());
        projectName.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");

        Label projectStatus = new Label("Status: " + p.getStatus());
        projectStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");

        Button delProjectBtn = new Button("Delete Project");
        delProjectBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-font-size:12;");
        delProjectBtn.setOnAction(e -> {
            try {
                ProjectDAO.deleteProject(p.getId());
                loadMilestones(); // refresh after deletion
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Failed to delete project: " + ex.getMessage());
            }
        });

        headerBox.getChildren().addAll(projectName, projectStatus, delProjectBtn);
        projectCard.getChildren().add(headerBox);

        // Milestones list
        VBox milestonesBox = new VBox(5);
        List<Milestone> milestones = MilestoneDAO.getMilestonesByProject(p.getId());

        for (Milestone m : milestones) {
            HBox milestoneBox = new HBox(10);
            milestoneBox.setAlignment(Pos.CENTER_LEFT);

            Label milestoneLabel = new Label(m.getName() + " (Due: " + m.getDueDate() + ")");
            milestoneLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#666;");

            Button delMilestoneBtn = new Button("Delete");
            delMilestoneBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-font-size:10;");
            delMilestoneBtn.setOnAction(ev -> {
                try {
                    MilestoneDAO.deleteMilestone(m.getId());
                    loadMilestones(); // refresh UI
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Failed to delete milestone: " + ex.getMessage());
                }
            });

            milestoneBox.getChildren().addAll(milestoneLabel, delMilestoneBtn);
            milestonesBox.getChildren().add(milestoneBox);
        }

        projectCard.getChildren().add(milestonesBox);
        projectsBox.getChildren().add(projectCard);
    }
}


// Utility method
private void showAlert(Alert.AlertType type, String msg) {
    Alert alert = new Alert(type);
    alert.setTitle("Milestones");
    alert.setHeaderText(null);
    alert.setContentText(msg);
    alert.showAndWait();
}

  private void loadDashboardData() {
    try {
        // Fetch projects from DB
        List<Project> projects = ProjectDAO.getAllProjects();

        int totalProjects = projects.size();
        int activeProjects = (int) projects.stream().filter(p -> "On Track".equals(p.getStatus())).count();
        int pendingProjects = (int) projects.stream().filter(p -> "At Risk".equals(p.getStatus())).count();
        double totalBudget = projects.stream().mapToDouble(Project::getBudget).sum();

        addCard(cardTotal, "Total Projects", String.valueOf(totalProjects), "#3498DB"); // blue
        addCard(cardActive, "Active Projects", String.valueOf(activeProjects), "#2ECC71"); // green
        addCard(cardPending, "Pending Projects", String.valueOf(pendingProjects), "#E74C3C"); // red
        addCard(cardBudget, "Total Budget", "$" + totalBudget, "#F1C40F"); // yellow

        // PieChart: Budget distribution
        if (budgetPie != null) {
            budgetPie.getData().clear();
            double labor = totalBudget * 0.45;
            double materials = totalBudget * 0.35;
            double equipment = totalBudget * 0.15;
            double misc = totalBudget * 0.05;

            PieChart.Data laborSlice = new PieChart.Data("Labor", 0);
            PieChart.Data materialsSlice = new PieChart.Data("Materials", 0);
            PieChart.Data equipmentSlice = new PieChart.Data("Equipment", 0);
            PieChart.Data miscSlice = new PieChart.Data("Misc", 0);

            budgetPie.getData().addAll(laborSlice, materialsSlice, equipmentSlice, miscSlice);

            // Animate slices sequentially
            Timeline timeline = new Timeline();

            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                    new KeyValue(laborSlice.pieValueProperty(), labor, Interpolator.EASE_BOTH)));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2),
                    new KeyValue(materialsSlice.pieValueProperty(), materials, Interpolator.EASE_BOTH)));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3),
                    new KeyValue(equipmentSlice.pieValueProperty(), equipment, Interpolator.EASE_BOTH)));
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(4),
                    new KeyValue(miscSlice.pieValueProperty(), misc, Interpolator.EASE_BOTH)));

            timeline.play();
        }

        // Upcoming Milestones
        if (milestonesBox != null) {
            milestonesBox.getChildren().clear();
            Label title = new Label("Upcoming Milestones");
            title.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
            milestonesBox.getChildren().add(title);

            List<Milestone> milestones = MilestoneDAO.getUpcomingMilestones();
            for (Milestone m : milestones) {
                Label lbl = new Label(m.getProjectName() + " (" + m.getStatus() + ") " + m.getDueDate());
                lbl.setStyle("-fx-font-size:14px; -fx-opacity:0;"); // start transparent
                milestonesBox.getChildren().add(lbl);

                FadeTransition ft = new FadeTransition(Duration.seconds(0.8), lbl);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.setDelay(Duration.millis(100 * milestonesBox.getChildren().indexOf(lbl)));
                ft.play();
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


   // Updated addCard method with color
private void addCard(VBox box, String title, String value, String bgColor) {
    if (box != null) {
        box.getChildren().clear();

        box.setStyle("-fx-background-color:" + bgColor + "; -fx-padding:15; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 14px; -fx-text-fill:white;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill:white;");

        box.getChildren().addAll(lblTitle, lblValue);
    }
}
@FXML
private void goCreateProject() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/create_project.fxml"));
        Parent root = loader.load();
        App.changeScene("Create Project", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
@FXML
private void goAssignResources() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/assign_resources.fxml"));
        Parent root = loader.load();
        App.changeScene("Assign Resources", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

   @FXML
private void goAssignWorkers() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/assign_workers.fxml"));
        Parent root = loader.load();
        App.changeScene("Assign Workers", new Scene(root), "/screens/ProjectManager/assign_workers.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

  @FXML
private void goRequestMaterial() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/request_material.fxml"));
        Parent root = loader.load();
        App.changeScene("Request Material", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

 @FXML
private void goProjectPhase() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/project_phase.fxml"));
        Parent root = loader.load();
        App.changeScene("Project Phase", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

 @FXML
private void goManageInventory() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/manage_inventory.fxml"));
        Parent root = loader.load();
        App.changeScene("Manage Inventory", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void goDoc() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/documents.fxml"));
        Parent root = loader.load();
        App.changeScene("Documents", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void goLog() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/daily_log.fxml"));
        Parent root = loader.load();
        App.changeScene("Daily Logs", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void goBudget() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/BudgetMonitor.fxml"));
        Parent root = loader.load();
        App.changeScene("Daily Logs", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void goNoti() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/screens/ProjectManager/notifications.fxml"));
        Parent root = loader.load();
        App.changeScene("Notification", new Scene(root), "/screens/ProjectManager/dashboard.css");
    } catch (Exception e) {
        e.printStackTrace();
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

   

    private void loadPage(String fileName) {
        if (mainContent != null) {
            try {
                Parent pane = FXMLLoader.load(getClass().getResource(fileName));
                mainContent.getChildren().clear();
                mainContent.getChildren().add(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setUsername(String username) {
        if (lblUsername != null && username != null) {
            lblUsername.setText(username);
        }
    }
}
