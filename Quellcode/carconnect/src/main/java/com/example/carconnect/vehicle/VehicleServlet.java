package com.example.carconnect.vehicle;

import com.example.carconnect.booking.BookingIf;
import com.example.carconnect.websocket.NotificationEndpoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet zur Verwaltung von Fahrzeugoperationen wie Suche, Aktualisierung, Löschung und Buchung.
 * Das Servlet kommuniziert über RMI mit dem Fahrzeug- und Buchungsservice und sendet Benachrichtigungen
 * über WebSocket-Verbindungen.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "VehicleServlet", urlPatterns = {"/vehicles"})
public class VehicleServlet extends HttpServlet {

    /** Remote-Referenz auf den Fahrzeugverwaltungsdienst */
    private VehicleIf vehicleIf;

    /** Remote-Referenz auf den Buchungsdienst */
    private BookingIf bookingIf;

    /**
     * Konstruktor für das VehicleServlet.
     *
     * @param bookingIf Remote-Referenz auf den Buchungsdienst, die vom Servlet verwendet wird.
     */
    public VehicleServlet(BookingIf bookingIf) {
        this.bookingIf = bookingIf;
    }

    // Hier kann eine Standardinitialisierung erfolgen

    public VehicleServlet() {
    }
    /**
     * Initialisiert das Servlet und stellt die Verbindung zum Fahrzeugverwaltungsdienst über RMI her.
     *
     * @throws ServletException Wenn die Verbindung zum RMI-Server fehlschlägt.
     */
    @Override
    public void init() throws ServletException {
        try {
            this.bookingIf = (BookingIf) Naming.lookup("rmi://localhost:1099/BookingIf");
            vehicleIf = (VehicleIf) Naming.lookup("rmi://localhost:1099/VehicleIf");
        } catch (Exception e) {
            throw new ServletException("Failed to lookup RMI server", e);
        }
    }

    /**
     * Verarbeitet GET-Anfragen, um eine Liste verfügbarer Fahrzeuge abzurufen und als JSON-Antwort zurückzugeben.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonResponse = new JSONObject();

        try {
            // Abrufen der verfügbaren Fahrzeuge
            List<Map<String, Object>> vehicles = vehicleIf.searchAvailableVehicles(new HashMap<>());
            JSONArray vehicleArray = new JSONArray();
            for (Map<String, Object> vehicle : vehicles) {
                JSONObject vehicleJson = new JSONObject(vehicle);
                vehicleArray.put(vehicleJson);
            }
            jsonResponse.put("vehicles", vehicleArray);
            System.out.println("Vehicles found: " + vehicleArray.length());
        } catch (RemoteException e) {
            jsonResponse.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        } catch (Exception e) {
            jsonResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }


    /**
     * Verarbeitet POST-Anfragen zur Verwaltung von Fahrzeugen, einschließlich Aktualisierung, Löschung und Buchung.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String username = request.getParameter("username");
        boolean result = false;
        JSONObject jsonResponse = new JSONObject();

        try {
            int vehicleId = Integer.parseInt(request.getParameter("vehicleId"));
            System.out.println("Received request for action: " + action + " by user: " + username + " for vehicleId: " + vehicleId);

            // Überprüfen, ob der Benutzer der Besitzer des Fahrzeugs ist oder eine Buchung vornehmen möchte
            if (vehicleIf.isVehicleOwner(username, vehicleId) || action.equals("book")) {
                switch (action) {
                    case "update":
                        // Fahrzeuginformationen aktualisieren
                        String make = request.getParameter("make");
                        String model = request.getParameter("model");
                        int year = Integer.parseInt(request.getParameter("year"));
                        String location = request.getParameter("location");
                        result = vehicleIf.updateVehicle(vehicleId, make, model, year, location);
                        if (result) {
                            // Senden einer Benachrichtigung über die Aktualisierung des Fahrzeugs
                            JSONObject updatedVehicle = new JSONObject();
                            updatedVehicle.put("vehicleId", vehicleId);
                            updatedVehicle.put("make", make);
                            updatedVehicle.put("model", model);
                            updatedVehicle.put("year", year);
                            updatedVehicle.put("location", location);
                            updatedVehicle.put("action", "update");

                            NotificationEndpoint.sendNotification(updatedVehicle.toString());
                        }
                        break;
                    case "delete":
                        // Fahrzeug löschen
                        result = vehicleIf.deleteVehicle(vehicleId);
                        if (result) {
                            // Senden einer Benachrichtigung über das Löschen des Fahrzeugs
                            JSONObject deletedVehicle = new JSONObject();
                            deletedVehicle.put("vehicleId", vehicleId);
                            deletedVehicle.put("action", "delete");

                            NotificationEndpoint.sendNotification(deletedVehicle.toString());
                        }
                        break;
                    case "book":
                        // Fahrzeug buchen
                        String startTime = request.getParameter("startTime");
                        String endTime = request.getParameter("endTime");
                        result = bookingIf.bookVehicle(username, vehicleId, startTime, endTime);
                        break;
                    default:
                        jsonResponse.put("error", "Invalid action");
                }
            } else {
                jsonResponse.put("error", "Unauthorized action");
                System.out.println("Unauthorized action attempted by user: " + username + " for vehicleId: " + vehicleId);
            }
        } catch (RemoteException e) {
            jsonResponse.put("error", e.getMessage());
        }

        jsonResponse.put("success", result);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
