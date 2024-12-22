package com.example.carconnect.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository-Klasse, die für den Zugriff auf die Datenbank und die Verwaltung von Buchungsdaten verantwortlich ist.
 * Diese Klasse enthält Methoden zum Erstellen, Aktualisieren und Abrufen von Buchungsanfragen sowie zur Aufzeichnung der Fahrzeugnutzung.
 *
 * @author Mohammed Al-Ozair
 * @author Nabeel Elamaireh
 */
public class BookingRepository {

    /** Verbindungsmanager für die Datenbank. */
    private final DBConnectionManager dbConnectionManager;

    /**
     * Konstruktor für das BookingRepository.
     *
     * @param dbConnectionManager Ein Manager, der die Datenbankverbindungen verwaltet.
     */
    public BookingRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    /**
     * Bucht ein Fahrzeug für einen Benutzer und fügt die Buchung in die Datenbank ein.
     *
     * @param username Der Benutzername des Nutzers, der die Buchung durchführt.
     * @param vehicleId Die ID des zu buchenden Fahrzeugs.
     * @param startTime Die Startzeit der Buchung.
     * @param endTime Die Endzeit der Buchung.
     * @return true, wenn die Buchung erfolgreich war, false andernfalls.
     */
    public boolean bookVehicle(String username, int vehicleId, String startTime, String endTime) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "INSERT INTO bookings (start_time, end_time, vehicle_id, username, status) VALUES (?, ?, ?, ?, 'PENDING')";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setTimestamp(1, Timestamp.valueOf(startTime));
            statement.setTimestamp(2, Timestamp.valueOf(endTime));
            statement.setInt(3, vehicleId);
            statement.setString(4, username);
            statement.execute();  // Ausführung des SQL-Befehls zum Einfügen der Buchung
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Rückgabe von false bei SQL-Ausnahme
        }
    }

    /**
     * Fügt eine neue Buchungsanfrage in die Datenbank ein.
     *
     * @param username Der Benutzername des Nutzers, der die Anfrage stellt.
     * @param vehicleId Die ID des Fahrzeugs, das angefragt wird.
     * @param startTime Die Startzeit der Buchung.
     * @param endTime Die Endzeit der Buchung.
     * @return true, wenn die Anfrage erfolgreich war, false andernfalls.
     */
    public boolean insertBookingRequest(String username, int vehicleId, String startTime, String endTime) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "INSERT INTO booking_requests (vehicle_id, requester, start_time, end_time, status) VALUES (?, ?, ?, ?, 'PENDING')";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, vehicleId);
            statement.setString(2, username);
            statement.setTimestamp(3, Timestamp.valueOf(startTime));
            statement.setTimestamp(4, Timestamp.valueOf(endTime));
            statement.execute();  // Ausführung des SQL-Befehls zum Einfügen der Buchungsanfrage
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Rückgabe von false bei SQL-Ausnahme
        }
    }

    /**
     * Aktualisiert den Status einer Buchungsanfrage und fügt bei Genehmigung die Nutzungshistorie ein.
     *
     * @param requestId Die ID der Buchungsanfrage, deren Status aktualisiert werden soll.
     * @param status Der neue Status der Buchungsanfrage (z.B. "APPROVED" oder "REJECTED").
     * @return true, wenn die Aktualisierung erfolgreich war, false andernfalls.
     */
    public boolean updateBookingRequestStatus(int requestId, String status) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "UPDATE booking_requests SET status = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, status);
            statement.setInt(2, requestId);
            int rowsAffected = statement.executeUpdate();  // Aktualisierung des Status der Buchungsanfrage

            if (status.equals("APPROVED") && rowsAffected > 0) {
                String usageQuery = "INSERT INTO usage_history (username, vehicle_id, start_time, end_time) " +
                        "SELECT requester, vehicle_id, start_time, end_time " +
                        "FROM booking_requests WHERE id = ?";
                PreparedStatement usageStatement = connection.prepareStatement(usageQuery);
                usageStatement.setInt(1, requestId);
                usageStatement.executeUpdate();  // Einfügen der Nutzungshistorie bei Genehmigung der Anfrage
            }

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Rückgabe von false bei SQL-Ausnahme
        }
    }

    /**
     * Ruft die Buchungsanfragen für einen bestimmten Benutzer ab.
     *
     * @param username Der Benutzername, für den die Anfragen abgerufen werden sollen.
     * @return Eine Liste von Maps, die die Details der Buchungsanfragen enthalten.
     */
    public List<Map<String, Object>> getBookingRequests(String username) {
        List<Map<String, Object>> bookingRequests = new ArrayList<>();
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT br.id, v.make || ' ' || v.model || ' (' || v.year || ')' as vehicle, br.requester, br.start_time, br.end_time, br.status " +
                    "FROM booking_requests br " +
                    "JOIN vehicles v ON br.vehicle_id = v.id " +
                    "WHERE v.owner_id = (SELECT id FROM users WHERE username = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("id", resultSet.getInt("id"));
                request.put("vehicle", resultSet.getString("vehicle"));
                request.put("requester", resultSet.getString("requester"));
                request.put("start_time", resultSet.getTimestamp("start_time").toString());
                request.put("end_time", resultSet.getTimestamp("end_time").toString());
                request.put("status", resultSet.getString("status"));
                bookingRequests.add(request);  // Hinzufügen der Buchungsanfrage zur Liste
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookingRequests;
    }

    /**
     * Ruft die Fahrzeug-ID basierend auf der Anfrage-ID ab.
     *
     * @param requestId Die ID der Buchungsanfrage.
     * @return Die Fahrzeug-ID, die mit der Buchungsanfrage verknüpft ist, oder -1 im Fehlerfall.
     */
    public int getVehicleIdFromBookingRequest(int requestId) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT vehicle_id FROM booking_requests WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, requestId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("vehicle_id");  // Rückgabe der Fahrzeug-ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Rückgabe von -1 im Fehlerfall
    }

    /**
     * Zeichnet die Nutzung eines Fahrzeugs in der Datenbank auf.
     *
     * @param username Der Benutzername des Nutzers, der das Fahrzeug nutzt.
     * @param vehicleId Die ID des genutzten Fahrzeugs.
     * @param startTime Die Startzeit der Nutzung.
     * @param endTime Die Endzeit der Nutzung.
     * @return true, wenn die Nutzung erfolgreich aufgezeichnet wurde, false andernfalls.
     */
    public boolean recordUsage(String username, int vehicleId, String startTime, String endTime) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "INSERT INTO usage_history (username, vehicle_id, start_time, end_time) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setInt(2, vehicleId);
            statement.setTimestamp(3, Timestamp.valueOf(startTime));
            statement.setTimestamp(4, Timestamp.valueOf(endTime));
            statement.execute();  // Ausführung des SQL-Befehls zum Einfügen der Nutzungshistorie
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Rückgabe von false bei SQL-Ausnahme
        }
    }

    /**
     * Ruft die Nutzungshistorie für einen bestimmten Benutzer ab.
     *
     * @param username Der Benutzername, für den die Nutzungshistorie abgerufen werden soll.
     * @return Eine Liste von Maps, die die Details der Nutzungshistorie enthalten.
     */
    public List<Map<String, Object>> getUsageHistory(String username) {
        List<Map<String, Object>> usageHistory = new ArrayList<>();
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT uh.vehicle_id, uh.start_time, uh.end_time, v.make, v.model, v.year " +
                    "FROM usage_history uh " +
                    "JOIN vehicles v ON uh.vehicle_id = v.id " +
                    "WHERE uh.username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> usage = new HashMap<>();
                usage.put("vehicleId", resultSet.getInt("vehicle_id"));
                usage.put("startTime", resultSet.getTimestamp("start_time").toString());
                usage.put("endTime", resultSet.getTimestamp("end_time").toString());
                usage.put("make", resultSet.getString("make"));
                usage.put("model", resultSet.getString("model"));
                usage.put("year", resultSet.getInt("year"));
                usageHistory.add(usage);  // Hinzufügen des Nutzungseintrags zur Liste
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usageHistory;
    }
}
