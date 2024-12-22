package com.example.carconnect.booking;

import com.example.carconnect.repository.BookingRepository;
import com.example.carconnect.repository.VehicleRepository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

/**
 * Implementierung der Buchungslogik, die die `BookingIf`-Schnittstelle über RMI bereitstellt.
 * Diese Klasse bietet Methoden zur Verwaltung von Fahrzeugbuchungen, Buchungsanfragen,
 * Genehmigungen, Ablehnungen und zur Aufzeichnung der Fahrzeugnutzung.
 *
 * @author Mohammed Al-Ozair
 * @author Nabeel Elamaireh
 */
public class BookingImpl extends UnicastRemoteObject implements BookingIf {

    /** Repository für Buchungsinformationen. */
    private final BookingRepository bookingRepository;

    /** Repository für Fahrzeuginformationen. */
    private final VehicleRepository vehicleRepository;

    /**
     * Konstruktor für BookingImpl.
     *
     * @param bookingRepository Das Repository zur Verwaltung von Buchungsinformationen.
     * @param vehicleRepository Das Repository zur Verwaltung von Fahrzeuginformationen.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    public BookingImpl(BookingRepository bookingRepository, VehicleRepository vehicleRepository) throws RemoteException {
        super();
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Bucht ein Fahrzeug für einen bestimmten Benutzer und zeichnet die Nutzung auf.
     *
     * @param username Der Benutzername des Nutzers, der die Buchung durchführt.
     * @param vehicleId Die ID des Fahrzeugs, das gebucht werden soll.
     * @param startTime Die Startzeit der Buchung.
     * @param endTime Die Endzeit der Buchung.
     * @return true, wenn das Fahrzeug erfolgreich gebucht wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean bookVehicle(String username, int vehicleId, String startTime, String endTime) throws RemoteException {
        try {
            boolean result = bookingRepository.bookVehicle(username, vehicleId, startTime, endTime);
            if (result) {
                bookingRepository.recordUsage(username, vehicleId, startTime, endTime);  // Aufzeichnung der Nutzung bei erfolgreicher Buchung
                System.out.println("Booking result for vehicle " + vehicleId + ": " + result);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error booking vehicle: " + e.getMessage(), e);  // Weiterleitung der Ausnahme als RemoteException
        }
    }

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
    @Override
    public boolean insertBookingRequest(String username, int vehicleId, String startTime, String endTime) throws RemoteException {
        return bookingRepository.insertBookingRequest(username, vehicleId, startTime, endTime);
    }

    /**
     * Genehmigt eine Buchungsanfrage basierend auf der Anfrage-ID.
     *
     * @param requestId Die ID der Buchungsanfrage, die genehmigt werden soll.
     * @return true, wenn die Buchungsanfrage erfolgreich genehmigt wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean approveBookingRequest(int requestId) throws RemoteException {
        boolean result = bookingRepository.updateBookingRequestStatus(requestId, "APPROVED");
        if (result) {
            int vehicleId = bookingRepository.getVehicleIdFromBookingRequest(requestId);
            vehicleRepository.updateVehicleAvailability(vehicleId, false);  // Setzt die Fahrzeugverfügbarkeit auf "nicht verfügbar"
        }
        return result;
    }

    /**
     * Lehnt eine Buchungsanfrage basierend auf der Anfrage-ID ab.
     *
     * @param requestId Die ID der Buchungsanfrage, die abgelehnt werden soll.
     * @return true, wenn die Buchungsanfrage erfolgreich abgelehnt wurde, false andernfalls.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public boolean rejectBookingRequest(int requestId) throws RemoteException {
        return bookingRepository.updateBookingRequestStatus(requestId, "REJECTED");
    }

    /**
     * Ruft eine Liste von Buchungsanfragen für einen bestimmten Benutzer ab.
     *
     * @param username Der Benutzername des Nutzers, für den die Buchungsanfragen abgerufen werden sollen.
     * @return Eine Liste von Maps, die die Details der Buchungsanfragen enthalten.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public List<Map<String, Object>> getBookingRequests(String username) throws RemoteException {
        return bookingRepository.getBookingRequests(username);
    }

    /**
     * Ruft die Fahrzeug-ID basierend auf der Anfrage-ID ab.
     *
     * @param requestId Die ID der Buchungsanfrage.
     * @return Die Fahrzeug-ID, die mit der Buchungsanfrage verknüpft ist.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public int getVehicleIdFromBookingRequest(int requestId) throws RemoteException {
        return bookingRepository.getVehicleIdFromBookingRequest(requestId);
    }

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
    @Override
    public boolean recordUsage(String username, int vehicleId, String startTime, String endTime) throws RemoteException {
        return bookingRepository.recordUsage(username, vehicleId, startTime, endTime);
    }

    /**
     * Ruft die Nutzungshistorie eines Benutzers ab.
     *
     * @param username Der Benutzername des Nutzers, für den die Nutzungshistorie abgerufen werden soll.
     * @return Eine Liste von Maps, die die Details der Nutzungshistorie enthalten.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    @Override
    public List<Map<String, Object>> getUsageHistory(String username) throws RemoteException {
        return bookingRepository.getUsageHistory(username);
    }
}
