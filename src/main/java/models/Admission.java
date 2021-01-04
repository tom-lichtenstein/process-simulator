package models;

import database.OracleConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Admission extends Model {

    static int globalIndex = 0;

    static int getIndex() {
        Admission.globalIndex = Admission.globalIndex + 1;
        return Admission.globalIndex;
    }

    public int id;
    public Patient patient;
    public List<Diagnosis> diagnoses;
    public List<Pharmacy> pharmacies;

    public Admission(Patient patient) {
        this.patient = patient;
        this.diagnoses = new ArrayList<>();
        this.pharmacies = new ArrayList<>();
    }

    public void addDiagnosis(Diagnosis diagnosis) {
        this.diagnoses.add(diagnosis);
    }

    public void addPharmacy(Pharmacy pharmacy) {
        this.pharmacies.add(pharmacy);
    }

    @Override
    public void create() throws SQLException {
        Connection con = OracleConnector.getConnection();
        Prescription.indexLock.lock();
        try {
            String SQL = "INSERT INTO admissions(id, subject_id) VALUES(?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            this.id = getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setInt(2, this.patient.id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created admission " + id + " for subject " + patient.id);
            } else {
                System.out.println("An error occurred creating admission " + id);
            }
        } finally {
            con.close();
            Admission.indexLock.unlock();
        }
    }

    @Override
    public void delete() throws SQLException {
        String SQL = "DELETE FROM admissions WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        con.close();
        if (affectedRows > 0) {
            System.out.println("Deleted admission " + id);
        } else {
            System.out.println("An error occurred deleting admission " + id);
        }
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String createTableSQL = "CREATE TABLE admissions (id INTEGER NOT NULL, subject_id INTEGER NULL, PRIMARY KEY ( id ), CONSTRAINT fk_admissions_subjects FOREIGN KEY(subject_id) REFERENCES subjects(id))";
        stmt.executeUpdate(createTableSQL);
        System.out.println("Created Admissions table");
    }

    public static void deleteTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String deleteTableSQL = "DROP TABLE admissions PURGE";
        try {
            stmt.executeUpdate(deleteTableSQL);
            System.out.println("Dropped Admissions table");
        } catch (SQLSyntaxErrorException e) {
            System.out.println("Could not delete Admissions");
        }
    }
}
