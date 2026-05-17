package model.ProcurementOfficer;
import model.ProjectManager.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class MaterialRequestManager {

    public static void processApprovedRequests() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String select = "SELECT id FROM material_requests WHERE status = 'Approved' AND processed = 0";
            try (PreparedStatement pst = conn.prepareStatement(select);
                 ResultSet rs = pst.executeQuery()) {
                MaterialRequestFacade facade = new MaterialRequestFacade();
                while (rs.next()) {
                    int reqId = rs.getInt("id");
                    try {
                        facade.approveRequest(reqId);
                    } catch (Exception e) {
                        // log and continue
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
