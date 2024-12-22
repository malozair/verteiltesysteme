package com.example.carconnect.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository-Klasse, die für den Zugriff auf die Benutzerdatenbank und die Verwaltung von Benutzerinformationen verantwortlich ist.
 * Diese Klasse enthält Methoden zur Überprüfung der Benutzerexistenz, zum Abrufen von Passwort-Hashes, zur Registrierung neuer Benutzer und zur Aktualisierung von Benutzerpasswörtern.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
public class UserRepository {

    /** Verbindungsmanager für die Datenbank. */
    private final DBConnectionManager dbConnectionManager;

    /**
     * Konstruktor für das UserRepository.
     *
     * @param dbConnectionManager Ein Manager, der die Datenbankverbindungen verwaltet.
     */
    public UserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    /**
     * Überprüft, ob ein Benutzer in der Datenbank existiert.
     *
     * @param username Der Benutzername, der überprüft werden soll.
     * @return true, wenn der Benutzer existiert, false andernfalls.
     */
    public boolean checkUserExistence(String username) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT username FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();
            return res.next();  // Rückgabe true, wenn der Benutzername gefunden wird
        } catch (SQLException e) {
            e.printStackTrace();  // Fehlerbehandlung bei SQL-Ausnahme
        }
        return false;
    }

    /**
     * Ruft den Passwort-Hash für einen bestimmten Benutzer ab.
     *
     * @param username Der Benutzername, dessen Passwort-Hash abgerufen werden soll.
     * @return Der Passwort-Hash als String, oder null, wenn der Benutzer nicht gefunden wird.
     */
    public String getHash(String username) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "SELECT hash FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getString("hash");  // Rückgabe des gefundenen Hashes
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Fehlerbehandlung bei SQL-Ausnahme
        }
        return null;  // Rückgabe null, wenn kein Hash gefunden wird
    }

    /**
     * Registriert einen neuen Benutzer in der Datenbank.
     *
     * @param username Der Benutzername des neuen Benutzers.
     * @param hash Der Passwort-Hash des neuen Benutzers.
     * @return true, wenn die Registrierung erfolgreich war, false andernfalls.
     */
    public boolean registerNewUser(String username, String hash) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "INSERT INTO users (username, hash) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, hash);
            statement.execute();  // Ausführung des SQL-Befehls zum Einfügen des neuen Benutzers
            return true;
        } catch (SQLException e) {
            e.printStackTrace();  // Fehlerbehandlung bei SQL-Ausnahme
        }
        return false;
    }

    /**
     * Aktualisiert den Passwort-Hash eines bestehenden Benutzers.
     *
     * @param username Der Benutzername des Benutzers, dessen Passwort-Hash aktualisiert werden soll.
     * @param newPasswordHash Der neue Passwort-Hash.
     * @return true, wenn die Aktualisierung erfolgreich war, false andernfalls.
     */
    public boolean updateUserPassword(String username, String newPasswordHash) {
        try {
            Connection connection = dbConnectionManager.getConnection();
            String query = "UPDATE users SET hash = ? WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newPasswordHash);
            statement.setString(2, username);
            int rowsUpdated = statement.executeUpdate();  // Ausführung des SQL-Befehls zur Aktualisierung des Passwort-Hashes
            return rowsUpdated > 0;  // Rückgabe true, wenn eine oder mehrere Zeilen aktualisiert wurden
        } catch (SQLException e) {
            e.printStackTrace();  // Fehlerbehandlung bei SQL-Ausnahme
            return false;
        }
    }
}
