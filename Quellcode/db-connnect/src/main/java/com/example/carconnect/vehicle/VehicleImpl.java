package com.example.carconnect.vehicle;

import com.example.carconnect.repository.VehicleRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

/**
 * Implementierung der `VehicleIf`-Schnittstelle, die über RMI verfügbar ist.
 * Diese Klasse stellt die Methoden zur Verwaltung von Fahrzeugen bereit und
 * verwendet das `VehicleRepository` für den Zugriff auf die Datenbank.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
public class VehicleImpl extends UnicastRemoteObject implements VehicleIf {

    /** Repository zur Verwaltung von Fahrzeuginformationen in der Datenbank. */
    private final VehicleRepository vehicleRepository;

    /**
     * Konstruktor für `VehicleImpl`.
     *
     * @param vehicleRepository Das Repository für den Datenbankzugriff auf Fahrzeuginformationen.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    public VehicleImpl(VehicleRepository vehicleRepository) throws RemoteException {
        super();
        this.vehicleRepository = vehicleRepository;
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
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean registerVehicle(String ownerUsername, String make, String model, int year, String location) throws RemoteException {
        return vehicleRepository.registerVehicle(ownerUsername, make, model, year, location);
    }

    /**
     * Sucht nach verfügbaren Fahrzeugen basierend auf den angegebenen Suchkriterien.
     *
     * @param searchCriteria Eine Map, die die Suchkriterien (z.B. Marke, Modell, Baujahr) enthält.
     * @return Eine Liste von Maps, die die Details der verfügbaren Fahrzeuge enthalten.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public List<Map<String, Object>> searchAvailableVehicles(Map<String, String> searchCriteria) throws RemoteException {
        return vehicleRepository.searchAvailableVehicles(searchCriteria);
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
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean updateVehicle(int vehicleId, String make, String model, int year, String location) throws RemoteException {
        return vehicleRepository.updateVehicle(vehicleId, make, model, year, location);
    }

    /**
     * Löscht ein Fahrzeug aus der Datenbank.
     *
     * @param vehicleId Die ID des Fahrzeugs, das gelöscht werden soll.
     * @return true, wenn das Fahrzeug erfolgreich gelöscht wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean deleteVehicle(int vehicleId) throws RemoteException {
        return vehicleRepository.deleteVehicle(vehicleId);
    }

    /**
     * Überprüft, ob ein bestimmter Benutzer der Besitzer eines Fahrzeugs ist.
     *
     * @param username Der Benutzername des potenziellen Fahrzeugbesitzers.
     * @param vehicleId Die ID des Fahrzeugs.
     * @return true, wenn der Benutzer der Besitzer des Fahrzeugs ist, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean isVehicleOwner(String username, int vehicleId) throws RemoteException {
        boolean isOwner = vehicleRepository.isVehicleOwner(username, vehicleId);
        System.out.println("AuthServerImpl.isVehicleOwner: " + isOwner + " for vehicleId: " + vehicleId + " and username: " + username);
        return isOwner;
    }

    /**
     * Ruft die ID des zuletzt eingefügten Fahrzeugs aus der Datenbank ab.
     *
     * @return Die ID des zuletzt eingefügten Fahrzeugs.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public int getLastInsertedVehicleId() throws RemoteException {
        return vehicleRepository.getLastInsertedVehicleId();
    }
}
