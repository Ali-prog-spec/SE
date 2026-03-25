package model.FinanceOfficer;
import model.ProjectManager.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class NewInvoice extends Invoice {

    private String project;
    private String client;
    private LocalDate date;
    private Double total;
    private double subtotal;
    private double totalTax;

    private final List<InvoiceItem> items;

    public NewInvoice(String project, String client, LocalDate date) {
        super(project, client, date); 
        this.project = project;
        this.client = client;
        this.date = date;
        this.total=0.0;
        this.items = new ArrayList<>();
    }

    // Getters and setters
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
    public double getTotal(){
        return total;
    }
    public double getSubtotal() {
        return subtotal;
    }

    public double getTotalTax() {
        return totalTax;
    }
    public List<InvoiceItem> getItems() {
        return items;
    }

    // Add an item to the invoice
    public void addItem(InvoiceItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    // Calculate total amount of invoice
    public double getTotalAmount() {
        return items.stream().mapToDouble(InvoiceItem::getTotal).sum();
    }
    public void clearItems() {
            items.clear();
        }

        public void calculateTotals() {
        subtotal = 0;
        totalTax = 0;
        for (InvoiceItem item : items) {
            double itemSubtotal = item.getQuantity() * item.getRate();
            double itemTax = itemSubtotal * item.getTax() / 100;
            subtotal += itemSubtotal;
            totalTax += itemTax;
        }
        total = subtotal + totalTax;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project: ").append(project).append("\n");
        sb.append("Client: ").append(client).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("Items:\n");
        for (InvoiceItem item : items) {
            sb.append(item).append("\n");
        }
        sb.append("Invoice Total: ").append(getTotalAmount()).append("\n");
        return sb.toString();
    }

    public void saveToDatabase() {
        String insertInvoiceSQL = "INSERT INTO invoices (project, client, date, total, subtotal, total_tax) VALUES (?, ?, ?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO invoice_items (invoice_id, item, quantity, rate, tax, total) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Insert invoice
            try (PreparedStatement ps = conn.prepareStatement(insertInvoiceSQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, project);
                ps.setString(2, client);
                ps.setDate(3, Date.valueOf(date));
                ps.setDouble(4, total);  
                ps.setDouble(5, subtotal);  
                ps.setDouble(6, totalTax); 
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int invoiceId = -1;
                if (rs.next()) invoiceId = rs.getInt(1);

                // Insert items
                try (PreparedStatement psItem = conn.prepareStatement(insertItemSQL)) {
                    for (InvoiceItem item : items) {
                        psItem.setInt(1, invoiceId);
                        psItem.setString(2, item.getItem());
                        psItem.setDouble(3, item.getQuantity());
                        psItem.setDouble(4, item.getRate());
                        psItem.setDouble(5, item.getTax());
                        psItem.setDouble(6, item.getTotal());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }
            }

            conn.commit(); // Commit transaction
            System.out.println("Invoice saved successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
