package com.example.carconnect.vehicle;

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
 * Servlet zur Fahrzeugsuche, das es ermöglicht, Fahrzeuge basierend auf bestimmten Suchkriterien zu suchen.
 * Dieses Servlet kommuniziert über RMI mit dem Fahrzeugverwaltungsdienst, um verfügbare Fahrzeuge abzurufen.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "VehicleSearchServlet", urlPatterns = {"/searchVehicles"})
public class VehicleSearchServlet extends HttpServlet {

    /** Remote-Referenz auf den Fahrzeugverwaltungsdienst. */
    private VehicleIf vehicleIf;

    /**
     * Initialisiert das Servlet und stellt die Verbindung zum Fahrzeugverwaltungsdienst über RMI her.
     *
     * @throws ServletException Wenn die Verbindung zum RMI-Server fehlschlägt.
     */
    @Override
    public void init() throws ServletException {
        try {
            vehicleIf = (VehicleIf) Naming.lookup("rmi://localhost:1099/VehicleIf");
        } catch (Exception e) {
            throw new ServletException("Failed to lookup RMI server", e);
        }
    }

    /**
     * Verarbeitet POST-Anfragen zur Suche nach verfügbaren Fahrzeugen basierend auf den angegebenen Suchkriterien.
     * Die Ergebnisse werden als JSON-Array zurückgegeben.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Extrahieren der Suchkriterien aus der Anfrage
        String model = request.getParameter("model");
        String make = request.getParameter("make");
        String year = request.getParameter("year");
        String location = request.getParameter("location");

        // Erstellen der Suchkriterien-Map basierend auf den bereitgestellten Parametern
        Map<String, String> searchCriteria = new HashMap<>();
        if (model != null && !model.isEmpty()) searchCriteria.put("model", model);
        if (make != null && !make.isEmpty()) searchCriteria.put("make", make);
        if (year != null && !year.isEmpty()) searchCriteria.put("year", year);
        if (location != null && !location.isEmpty()) searchCriteria.put("location", location);

        JSONArray vehiclesArray = new JSONArray();
        try {
            // Abrufen der verfügbaren Fahrzeuge basierend auf den Suchkriterien
            List<Map<String, Object>> vehicles = vehicleIf.searchAvailableVehicles(searchCriteria);
            for (Map<String, Object> vehicle : vehicles) {
                JSONObject vehicleJson = new JSONObject();
                vehicleJson.put("make", vehicle.get("make"));
                vehicleJson.put("model", vehicle.get("model"));
                vehicleJson.put("year", vehicle.get("year"));
                vehicleJson.put("location", vehicle.get("location"));
                vehicleJson.put("ownerName", vehicle.get("ownerName"));
                vehiclesArray.put(vehicleJson); // Hinzufügen des Fahrzeugs zur JSON-Antwort
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(vehiclesArray.toString());
    }
}
