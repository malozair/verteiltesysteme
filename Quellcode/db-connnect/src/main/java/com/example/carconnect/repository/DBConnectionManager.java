package com.example.carconnect.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnectionManager ist eine Klasse, die eine Verbindung zur Datenbank verwaltet.
 * Sie stellt sicher, dass nur eine einzige Instanz der Datenbankverbindung verwendet wird (Singleton-Pattern).
 *
 * Diese Klasse verwendet PostgreSQL als Datenbankmanagementsystem.
 *
 * @author Mohammed Al-Ozair
 * @author Nabeel Elamaireh
 */
public class DBConnectionManager {

    /** Die URL der Datenbankverbindung. */
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/carconnectdb";

    /** Der Benutzername für die Datenbankverbindung. */
    private static final String USER = "carconnectuser";

    /** Das Passwort für die Datenbankverbindung. */
    private static final String PASSWORD = "12345";

    /** Die Singleton-Instanz der Datenbankverbindung. */
    private static Connection connection;

    /**
     * Konstruktor für DBConnectionManager.
     * Ruft die Datenbankverbindung ab und stellt sicher, dass sie initialisiert ist.
     */
    public DBConnectionManager() {
        getConnection();  // Verbindungsaufbau bei Instanzierung des Managers
    }

    /**
     * Stellt eine Verbindung zur Datenbank her, falls diese noch nicht existiert.
     *
     * @return Die Singleton-Instanz der Datenbankverbindung.
     */
    public static Connection getConnection() {
        if (connection == null) {  // Überprüft, ob die Verbindung bereits besteht
            try {
                Class.forName("org.postgresql.Driver");  // Lädt den PostgreSQL JDBC-Treiber
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);  // Baut die Verbindung zur Datenbank auf
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();  // Fehlerbehandlung bei Problemen mit dem Treiber oder der Verbindung
            }
        }
        return connection;  // Rückgabe der bestehenden oder neu erstellten Verbindung
    }
}
