package com.example.carconnect.vehicle;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Schnittstelle für Fahrzeugverwaltungsoperationen, die über RMI (Remote Method Invocation) verfügbar sind.
 * Diese Schnittstelle definiert Methoden zur Registrierung, Suche, Aktualisierung und Löschung von Fahrzeugen
 * sowie zur Überprüfung der Fahrzeugbesitzer und zum Abrufen der letzten eingefügten Fahrzeug-ID.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
public interface VehicleIf extends Remote {

    /**
     * Registriert ein neues Fahrzeug.
     *
     * @param ownerUsername Der Benutzername des Fahrzeugbesitzers.
     * @param make Die Marke des Fahrzeugs.
     * @param model Das Modell des Fahrzeugs.
     * @param year Das Baujahr des Fahrzeugs.
     * @param location Der Standort des Fahrzeugs.
     * @return true, wenn das Fahrzeug erfolgreich registriert wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean registerVehicle(String ownerUsername, String make, String model, int year, String location) throws RemoteException;

    /**
     * Sucht nach verfügbaren Fahrzeugen basierend auf den angegebenen Suchkriterien.
     *
     * @param searchCriteria Eine Map, die die Suchkriterien (z.B. Marke, Modell, Baujahr) enthält.
     * @return Eine Liste von Maps, die die Details der verfügbaren Fahrzeuge enthalten.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    List<Map<String, Object>> searchAvailableVehicles(Map<String, String> searchCriteria) throws RemoteException;

    /**
     * Aktualisiert die Informationen eines Fahrzeugs.
     *
     * @param vehicleId Die ID des Fahrzeugs, das aktualisiert werden soll.
     * @param make Die neue Marke des Fahrzeugs.
     * @param model Das neue Modell des Fahrzeugs.
     * @param year Das neue Baujahr des Fahrzeugs.
     * @param location Der neue Standort des Fahrzeugs.
     * @return true, wenn die Aktualisierung erfolgreich war, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean updateVehicle(int vehicleId, String make, String model, int year, String location) throws RemoteException;

    /**
     * Löscht ein Fahrzeug.
     *
     * @param vehicleId Die ID des Fahrzeugs, das gelöscht werden soll.
     * @return true, wenn das Fahrzeug erfolgreich gelöscht wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean deleteVehicle(int vehicleId) throws RemoteException;

    /**
     * Überprüft, ob ein bestimmter Benutzer der Besitzer eines Fahrzeugs ist.
     *
     * @param username Der Benutzername des potenziellen Fahrzeugbesitzers.
     * @param vehicleId Die ID des Fahrzeugs.
     * @return true, wenn der Benutzer der Besitzer des Fahrzeugs ist, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean isVehicleOwner(String username, int vehicleId) throws RemoteException;

    /**
     * Ruft die ID des zuletzt eingefügten Fahrzeugs ab.
     *
     * @return Die ID des zuletzt eingefügten Fahrzeugs.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    int getLastInsertedVehicleId() throws RemoteException;
}
