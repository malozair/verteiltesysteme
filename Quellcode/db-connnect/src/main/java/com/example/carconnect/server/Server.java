package com.example.carconnect.server;

import com.example.carconnect.auth.AuthIf;
import com.example.carconnect.auth.AuthImpl;
import com.example.carconnect.booking.BookingIf;
import com.example.carconnect.booking.BookingImpl;
import com.example.carconnect.repository.DBConnectionManager;
import com.example.carconnect.repository.UserRepository;
import com.example.carconnect.repository.VehicleRepository;
import com.example.carconnect.repository.BookingRepository;
import com.example.carconnect.vehicle.VehicleIf;
import com.example.carconnect.vehicle.VehicleImpl;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Hauptklasse für den RMI-Server, die die verschiedenen Services für die Authentifizierung,
 * Fahrzeugverwaltung und Buchungen bereitstellt. Diese Klasse startet das RMI-Registry,
 * initialisiert die notwendigen Repositories und bindet die Services an die RMI-Registry.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
public class Server {

    /** Der Port, auf dem der RMI-Server läuft. */
    private static final int PORT = 1099;

    /**
     * Hauptmethode, die den Server startet, indem sie das RMI-Registry initialisiert und die
     * verschiedenen Services (Auth, Vehicle, Booking) bindet.
     *
     * @param args Kommandozeilenargumente (werden nicht verwendet).
     */
    public static void main(String[] args) {
        try {
            // Starten des RMI-Registry
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(PORT);
                registry.list();  // Überprüft, ob das Registry bereits läuft
            } catch (RemoteException e) {
                registry = LocateRegistry.createRegistry(PORT);  // Erstellt das Registry, falls es noch nicht läuft
            }

            // Initialisierung des DBConnectionManager
            DBConnectionManager dbConnectionManager = new DBConnectionManager();

            // Initialisierung der Repositories
            UserRepository userRepository = new UserRepository(dbConnectionManager);
            VehicleRepository vehicleRepository = new VehicleRepository(dbConnectionManager);
            BookingRepository bookingRepository = new BookingRepository(dbConnectionManager);

            // Initialisierung und Bindung des Authentifizierungsdienstes
            AuthIf authService = new AuthImpl(userRepository);
            Naming.rebind("rmi://localhost:" + PORT + "/AuthIf", authService);

            // Initialisierung und Bindung des Fahrzeugverwaltungsdienstes
            VehicleIf vehicleService = new VehicleImpl(vehicleRepository);
            Naming.rebind("rmi://localhost:" + PORT + "/VehicleIf", vehicleService);

            // Initialisierung und Bindung des Buchungsdienstes
            BookingIf bookingService = new BookingImpl(bookingRepository, vehicleRepository);
            Naming.rebind("rmi://localhost:" + PORT + "/BookingIf", bookingService);

            System.out.println("Server is ready on port " + PORT);

        } catch (Exception e) {
            e.printStackTrace();  // Fehlerbehandlung bei Ausnahmen
        }
    }
}
