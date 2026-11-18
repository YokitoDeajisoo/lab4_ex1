package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:family_doctor.db";

    public DatabaseManager() {
        initDatabase();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS patients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "birth_date DATE NOT NULL, " +
                "weight DOUBLE NOT NULL, " +
                "has_declaration BOOLEAN NOT NULL, " +
                "diagnosis TEXT NOT NULL" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);

            // Перевіряємо, чи є записи; якщо таблиця порожня — додаємо 4 початкові записи
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patients");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertInitialSQL = "INSERT INTO patients (full_name, birth_date, weight, has_declaration, diagnosis) VALUES " +
                        "('Коваленко Олександр Іванович', '1985-04-12', 82.5, 1, 'Гіпертонія 1 ступеня'), " +
                        "('Мельник Марія Петрівна', '1992-11-23', 61.0, 1, 'Здорова, плановий огляд'), " +
                        "('Бондаренко Тарас Григорович', '1978-07-05', 94.2, 0, 'ГРВІ, гострий бронхіт'), " +
                        "('Шевченко Ірина Василівна', '2001-02-18', 54.8, 1, 'Алергічний риніт');";
                stmt.executeUpdate(insertInitialSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Patient> getAllPatients(String orderByColumn, boolean ascending) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String direction = ascending ? "ASC" : "DESC";
        String query = "SELECT * FROM patients ORDER BY " + orderByColumn + " " + direction;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(extractPatient(rs));
            }
        }
        return list;
    }

    public List<Patient> searchPatients(String field, String keyword) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT * FROM patients WHERE " + field + " LIKE ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractPatient(rs));
            }
        }
        return list;
    }

    public void addPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (full_name, birth_date, weight, has_declaration, diagnosis) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getFullName());
            pstmt.setString(2, p.getBirthDate());
            pstmt.setDouble(3, p.getWeight());
            pstmt.setBoolean(4, p.isHasDeclaration());
            pstmt.setString(5, p.getDiagnosis());
            pstmt.executeUpdate();
        }
    }

    public void updatePatient(Patient p) throws SQLException {
        String sql = "UPDATE patients SET full_name = ?, birth_date = ?, weight = ?, has_declaration = ?, diagnosis = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getFullName());
            pstmt.setString(2, p.getBirthDate());
            pstmt.setDouble(3, p.getWeight());
            pstmt.setBoolean(4, p.isHasDeclaration());
            pstmt.setString(5, p.getDiagnosis());
            pstmt.setInt(6, p.getId());
            pstmt.executeUpdate();
        }
    }

    public void deletePatient(int id) throws SQLException {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Patient extractPatient(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("birth_date"),
                rs.getDouble("weight"),
                rs.getBoolean("has_declaration"),
                rs.getString("diagnosis")
        );
    }
}