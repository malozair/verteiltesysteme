package com.example.carconnect.booking;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Schnittstelle für Buchungsoperationen, die über RMI (Remote Method Invocation) verfügbar sind.
 * Diese Schnittstelle definiert Methoden zur Verwaltung von Buchungsanfragen, Genehmigungen, Ablehnungen
 * sowie zur Aufzeichnung der Fahrzeugnutzung und zum Abrufen von Nutzungsverlauf.
 *
 * @author Mohammed Al-Ozair
 * @author Nabeel Elamaireh
 */
public interface BookingIf extends Remote {

    /**
     * Fügt eine neue Buchungsanfrage für ein Fahrzeug hinzu.
     *
     * @param username Der Benutzername des Nutzers, der die Buchung anfragt.
     * @param vehicleId Die ID des Fahrzeugs, das gebucht werden soll.
     * @param startTime Die Startzeit der Buchung.
     * @param endTime Die Endzeit der Buchung.
     * @return true, wenn die Buchungsanfrage erfolgreich hinzugefügt wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean insertBookingRequest(String username, int vehicleId, String startTime, String endTime) throws RemoteException;

    /**
     * Genehmigt eine Buchungsanfrage basierend auf der Anfrage-ID.
     *
     * @param requestId Die ID der Buchungsanfrage, die genehmigt werden soll.
     * @return true, wenn die Buchungsanfrage erfolgreich genehmigt wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean approveBookingRequest(int requestId) throws RemoteException;

    /**
     * Lehnt eine Buchungsanfrage basierend auf der Anfrage-ID ab.
     *
     * @param requestId Die ID der Buchungsanfrage, die abgelehnt werden soll.
     * @return true, wenn die Buchungsanfrage erfolgreich abgelehnt wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean rejectBookingRequest(int requestId) throws RemoteException;

    /**
     * Ruft eine Liste von Buchungsanfragen für einen bestimmten Benutzer ab.
     *
     * @param username Der Benutzername des Nutzers, für den die Buchungsanfragen abgerufen werden sollen.
     * @return Eine Liste von Maps, die die Details der Buchungsanfragen enthalten.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    List<Map<String, Object>> getBookingRequests(String username) throws RemoteException;

    /**
     * Ruft die Fahrzeug-ID basierend auf der Anfrage-ID ab.
     *
     * @param requestId Die ID der Buchungsanfrage.
     * @return Die Fahrzeug-ID, die mit der Buchungsanfrage verknüpft ist.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    int getVehicleIdFromBookingRequest(int requestId) throws RemoteException;

    /**
     * Bucht ein Fahrzeug für einen bestimmten Benutzer.
     *
     * @param username Der Benutzername des Nutzers, der die Buchung durchführt.
     * @param vehicleId Die ID des Fahrzeugs, das gebucht werden soll.
     * @param startTime Die Startzeit der Buchung.
     * @param endTime Die Endzeit der Buchung.
     * @return true, wenn das Fahrzeug erfolgreich gebucht wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean bookVehicle(String username, int vehicleId, String startTime, String endTime) throws RemoteException;

    /**
     * Zeichnet die Nutzung eines Fahrzeugs durch einen Benutzer auf.
     *
     * @param username Der Benutzername des Nutzers, der das Fahrzeug nutzt.
     * @param vehicleId Die ID des genutzten Fahrzeugs.
     * @param startTime Die Startzeit der Nutzung.
     * @param endTime Die Endzeit der Nutzung.
     * @return true, wenn die Nutzung erfolgreich aufgezeichnet wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    boolean recordUsage(String username, int vehicleId, String startTime, String endTime) throws RemoteException;

    /**
     * Ruft den Nutzungshistorie eines Benutzers ab.
     *
     * @param username Der Benutzername des Nutzers, für den die Nutzungshistorie abgerufen werden soll.
     * @return Eine Liste von Maps, die die Details der Nutzungshistorie enthalten.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    List<Map<String, Object>> getUsageHistory(String username) throws RemoteException;
}
