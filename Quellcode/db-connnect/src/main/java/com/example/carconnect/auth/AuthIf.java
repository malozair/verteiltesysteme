package com.example.carconnect.auth;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Schnittstelle für Authentifizierungsoperationen, die über RMI (Remote Method Invocation) verfügbar sind.
 * Diese Schnittstelle definiert Methoden zur Verwaltung von Benutzersitzungen und -anmeldungen.
 *
 * @author Mohammed Al-Ozair
 * @author Nabeel Elamaireh
 */
public interface AuthIf extends Remote {

    /**
     * Generiert eine neue Sitzungs-ID für einen Benutzer basierend auf dem Benutzernamen.
     *
     * @param username Der Benutzername, für den eine neue Sitzungs-ID erstellt werden soll.
     * @return Eine neue eindeutige Sitzungs-ID als long-Wert.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    long generateNewSessionId(String username) throws RemoteException;

    /**
     * Überprüft, ob ein Benutzer anhand der Sitzungs-ID und eines Hashes gültig ist.
     *
     * @param sessionId Die Sitzungs-ID, die überprüft werden soll.
     * @param hash Ein Hashwert zur Validierung des Benutzers.
     * @return true, wenn der Benutzer validiert werden konnte, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean validateUser(long sessionId, String hash) throws RemoteException;

    /**
     * Registriert einen neuen Benutzer mit einem Benutzernamen und einem Passwort-Hash.
     *
     * @param username Der Benutzername des neuen Benutzers.
     * @param hash Der Hash des Passworts für den neuen Benutzer.
     * @return true, wenn die Registrierung erfolgreich war, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean registerUser(String username, String hash) throws RemoteException;

    /**
     * Ändert das Passwort eines bestehenden Benutzers.
     *
     * @param username Der Benutzername des Benutzers, dessen Passwort geändert werden soll.
     * @param oldPassword Das alte Passwort des Benutzers.
     * @param newPassword Das neue Passwort, das gesetzt werden soll.
     * @return true, wenn das Passwort erfolgreich geändert wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean changePassword(String username, String oldPassword, String newPassword) throws RemoteException;
}
