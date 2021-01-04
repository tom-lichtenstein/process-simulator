package models;

import database.OracleConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pharmacy extends Model {

    static int globalIndex = 0;

    static int getIndex() {
        Pharmacy.globalIndex = Pharmacy.globalIndex + 1;
        return Pharmacy.globalIndex;
    }

    public int id;
    public String frequency;
    public Admission admission;
    public List<Prescription> prescriptions;

    public Pharmacy(Admission admission, String frequency) {
        this.frequency = frequency;
        this.admission = admission;
        this.prescriptions = new ArrayList<>();
    }

    public void addPrescription(Prescription prescription) {
        this.prescriptions.add(prescription);
    }

    @Override
    public void create() throws SQLException {
        Connection con = OracleConnector.getConnection();
        Prescription.indexLock.lock();
        try {
            String SQL = "INSERT INTO pharmacy(id, admission_id, frequency) VALUES(?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            this.id = getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setInt(2, this.admission.id);
            pstmt.setString(3, this.frequency);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created pharmacy " + id + " for admission " + admission.id);
            } else {
                System.out.println("An error occurred creating pharmacy " + id);
            }
        } finally {
            con.close();
            Pharmacy.indexLock.unlock();
        }
    }

    @Override
    public void delete() throws SQLException {
        String SQL = "DELETE FROM pharmacy WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted pharmacy " + id);
        } else {
            System.out.println("An error occurred deleting pharmacy " + id);
        }
        con.close();
    }

    public void update() throws SQLException {
        String[] frequencies = new String[] {"DAILY", "WEEKLY", "MONTHLY", "3 TIMES", "10 DAYS", "ONCE"};
        Random r = new Random();
        String frequency = frequencies[r.nextInt(frequencies.length - 1)];
        String SQL = "UPDATE pharmacy SET frequency = ? WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, frequency);
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated pharmacy " + id);
        } else {
            System.out.println("An error occurred updating pharmacy " + id);
        }
        con.close();
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String createTableSQL = "CREATE TABLE pharmacy (id INTEGER NOT NULL, admission_id INTEGER NULL, frequency VARCHAR(64), PRIMARY KEY ( id ), CONSTRAINT fk_pharmacy_admissions FOREIGN KEY(admission_id) REFERENCES admissions(id))";
        stmt.executeUpdate(createTableSQL);
        System.out.println("Created Pharmacy table");
    }

    public static void deleteTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String deleteTableSQL = "DROP TABLE pharmacy PURGE";
        try {
            stmt.executeUpdate(deleteTableSQL);
            System.out.println("Dropped Pharmacy table");
        } catch (SQLSyntaxErrorException e) {
            System.out.println("Could not delete Pharmacy");
        }
    }
}
