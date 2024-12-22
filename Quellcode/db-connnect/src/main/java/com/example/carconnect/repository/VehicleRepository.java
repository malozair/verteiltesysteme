package com.example.carconnect.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository-Klasse, die für den Zugriff auf die Fahrzeugdatenbank und die Verwaltung von Fahrzeuginformationen verantwortlich ist.
 * Diese Klasse enthält Methoden zum Registrieren, Suchen, Aktualisieren, Löschen und Überprüfen der Verfügbarkeit von Fahrzeugen.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
public class VehicleRepository {

    /** Verbindungsmanager für die Datenbank. */
    private final DBConnectionManager dbConnectionManager;

    /**
     * Konstruktor für das VehicleRepository.
     *
     * @param dbConnectionManager Ein Manager, der die Datenbankverbindungen verwaltet.
     */
    public VehicleRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    /**
     * Registriert ein neues Fahrzeug in der Datenbank.
     *
     * @param ownerUsername Der Benutzername des Fahrzeugbesitzers.
     * @param make Die Marke des Fahrzeugs.
     * @param model Das Modell des Fahrzeugs.
     * @param year Das Baujahr des Fahrzeugs.
     * @param location Der Standort des Fahrzeugs.
     * @return true, wenn das Fahrzeug erfolgreich registriert wurde, false andernfalls.
     */
    public boolean registerVehicle(String ownerUsername, String make, String model, int year, String location) {
        try {
            Connection connection = dbConnectionManager.getConnection();

            // Abrufen der Benutzer-ID basierend auf dem Benutzernamen
            String userQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement userStatement = connection.prepareStatement(userQuery);
            userStatement.setString(1, ownerUsername);
            ResultSet userResult = userStatement.executeQuery();

            if (userResult.next()) {
                int ownerId = userResult.getInt("id");

                // Einfügen des Fahrzeugs in die Datenbank
                String query = "INSERT INTO vehicles (owner_id, make, model, year, location, available) VALUES (?, ?, ?, ?, ?, true)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, ownerId);
                statement.setString(2, make);
                statement.setString(3, model);
                statement.setInt(4, year);
                statement.setString(5, location);
                statement.execute();
                return true;
            } else {
                return false; // Rückgabe false, wenn der Benutzer nicht existiert
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return false;
    }

    /**
     * Sucht nach verfügbaren Fahrzeugen basierend auf den angegebenen Suchkriterien.
     *
     * @param searchCriteria Eine Map, die die Suchkriterien (z.B. Marke, Modell, Baujahr) enthält.
     * @return Eine Liste von Maps, die die Details der verfügbaren Fahrzeuge enthalten.
     */
    public List<Map<String, Object>> searchAvailableVehicles(Map<String, String> searchCriteria) {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        try {
            Connection connection = dbConnectionManager.getConnection();

            // Erstellung des SQL-Queries basierend auf den Suchkriterien
            StringBuilder query = new StringBuilder("SELECT v.*, u.username AS owner_name FROM vehicles v JOIN users u ON v.owner_id = u.id WHERE v.available = true");

            for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
                if (entry.getKey().equals("year")) {
                    query.append(" AND ").append(entry.getKey()).append(" = ?");
                } else {
                    query.append(" AND LOWER(").append(entry.getKey()).append(") LIKE ?");
                }
            }

            PreparedStatement statement = connection.prepareStatement(query.toString());

            int index = 1;
            for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
                if (entry.getKey().equals("year")) {
                    statement.setInt(index++, Integer.parseInt(entry.getValue()));
                } else {
                    statement.setString(index++, "%" + entry.getValue() + "%");
                }
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> vehicle = new HashMap<>();
                vehicle.put("id", resultSet.getInt("id"));
                vehicle.put("ownerName", resultSet.getString("owner_name"));
                vehicle.put("make", resultSet.getString("make"));
                vehicle.put("model", resultSet.getString("model"));
                vehicle.put("year", resultSet.getInt("year"));
                vehicle.put("location", resultSet.getString("location"));
                vehicle.put("available", resultSet.getBoolean("available"));
                vehicles.add(vehicle); // Hinzufügen des Fahrzeugs zur Liste der Suchergebnisse
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return vehicles;
    }

    /**
     * Aktualisiert die Informationen eines Fahrzeugs in der Datenbank.
     *
     * @param vehicleId Die ID des Fahrzeugs, das aktualisiert werden soll.
     * @param make Die neue Marke des Fahrzeugs.
     * @param model Das neue Modell des Fahrzeugs.
     * @param year Das neue Baujahr des Fahrzeugs.
     * @param location Der neue Standort des Fahrzeugs.
     * @return true, wenn die Aktualisierung erfolgreich war, false andernfalls.
     */
    public boolean updateVehicle(int vehicleId, String make, String model, int year, String location) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "UPDATE vehicles SET make = ?, model = ?, year = ?, location = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, make);
            statement.setString(2, model);
            statement.setInt(3, year);
            statement.setString(4, location);
            statement.setInt(5, vehicleId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return false;
    }

    /**
     * Löscht ein Fahrzeug aus der Datenbank, einschließlich aller zugehörigen Buchungsanfragen.
     *
     * @param vehicleId Die ID des Fahrzeugs, das gelöscht werden soll.
     * @return true, wenn das Fahrzeug erfolgreich gelöscht wurde, false andernfalls.
     */
    public boolean deleteVehicle(int vehicleId) {
        try {
            Connection connection = dbConnectionManager.getConnection();

            // Löschen der zugehörigen Buchungsanfragen
            String deleteBookingRequestsQuery = "DELETE FROM booking_requests WHERE vehicle_id = ?";
            PreparedStatement deleteBookingRequestsStatement = connection.prepareStatement(deleteBookingRequestsQuery);
            deleteBookingRequestsStatement.setInt(1, vehicleId);
            deleteBookingRequestsStatement.executeUpdate();

            // Löschen des Fahrzeugs
            String query = "DELETE FROM vehicles WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, vehicleId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return false;
    }

    /**
     * Aktualisiert die Verfügbarkeit eines Fahrzeugs in der Datenbank.
     *
     * @param vehicleId Die ID des Fahrzeugs, dessen Verfügbarkeit aktualisiert werden soll.
     * @param available Der neue Verfügbarkeitsstatus des Fahrzeugs.
     * @return true, wenn die Aktualisierung erfolgreich war, false andernfalls.
     */
    public boolean updateVehicleAvailability(int vehicleId, boolean available) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "UPDATE vehicles SET available = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBoolean(1, available);
            statement.setInt(2, vehicleId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
            return false;
        }
    }

    /**
     * Überprüft, ob ein bestimmter Benutzer der Besitzer eines Fahrzeugs ist.
     *
     * @param username Der Benutzername des potenziellen Fahrzeugbesitzers.
     * @param vehicleId Die ID des Fahrzeugs.
     * @return true, wenn der Benutzer der Besitzer des Fahrzeugs ist, false andernfalls.
     */
    public boolean isVehicleOwner(String username, int vehicleId) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT owner_id FROM vehicles WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, vehicleId);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                int ownerId = res.getInt("owner_id");
                String ownerQuery = "SELECT username FROM users WHERE id = ?";
                PreparedStatement ownerStatement = connection.prepareStatement(ownerQuery);
                ownerStatement.setInt(1, ownerId);
                ResultSet ownerRes = ownerStatement.executeQuery();
                if (ownerRes.next()) {
                    return ownerRes.getString("username").equals(username); // Rückgabe true, wenn der Benutzername übereinstimmt
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return false;
    }

    /**
     * Ruft die ID des zuletzt eingefügten Fahrzeugs ab.
     *
     * @return Die ID des zuletzt eingefügten Fahrzeugs, oder -1 im Fehlerfall.
     */
    public int getLastInsertedVehicleId() {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT currval(pg_get_serial_sequence('vehicles', 'id'))";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1); // Rückgabe der zuletzt eingefügten Fahrzeug-ID
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return -1;
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
                return resultSet.getInt("vehicle_id"); // Rückgabe der Fahrzeug-ID
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung bei SQL-Ausnahme
        }
        return -1;
    }
}
