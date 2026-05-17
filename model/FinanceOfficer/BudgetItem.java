package model.FinanceOfficer;
import javafx.beans.property.*;

public class BudgetItem {
    private final StringProperty project;
    private final DoubleProperty budget;
    private final DoubleProperty spent;
    private final DoubleProperty remaining;
    private final DoubleProperty utilization;

    public BudgetItem(String project, double budget, double spent) {
    this.project = new SimpleStringProperty(project);
    this.budget = new SimpleDoubleProperty(budget);
    this.spent = new SimpleDoubleProperty(spent);

    double remainingValue = budget - spent;
    double utilValue = (budget == 0) ? 0 : (spent / budget) * 100;

    this.remaining = new SimpleDoubleProperty(remainingValue);
    this.utilization = new SimpleDoubleProperty(utilValue);
}


    public String getProject() { return project.get(); }
    public void setProject(String value) { project.set(value); }
    public StringProperty projectProperty() { return project; }

    public double getBudget() { return budget.get(); }
    public void setBudget(double value) { budget.set(value); }
    public DoubleProperty budgetProperty() { return budget; }

    public double getSpent() { return spent.get(); }
    public void setSpent(double value) { spent.set(value); }
    public DoubleProperty spentProperty() { return spent; }


    public double getRemaining() { return remaining.get(); }
    public double getUtilization() { return utilization.get(); }    

    public DoubleProperty remainingProperty() { return remaining; }
        public DoubleProperty utilizationProperty() { return utilization; }
    public void recalculate() {
           double remain = budget.get() - spent.get();
        remaining.set(Math.max(remain, 0));

        double percent = (budget.get() == 0) ? 0 : (spent.get() / budget.get()) * 100;
        utilization.set(percent); // store numeric value
        }

}
