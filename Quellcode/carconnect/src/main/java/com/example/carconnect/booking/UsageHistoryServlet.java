package com.example.carconnect.booking;

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
import java.util.List;
import java.util.Map;

/**
 * Servlet zur Verwaltung der Nutzungshistorie von Fahrzeugen eines Benutzers.
 * Dieses Servlet ermöglicht es, die Nutzungshistorie abzurufen und neue Nutzungsdaten zu speichern,
 * indem es über RMI mit dem Buchungsdienst kommuniziert.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "UsageHistoryServlet", urlPatterns = {"/usageHistory"})
public class UsageHistoryServlet extends HttpServlet {

    /** Remote-Referenz auf den Buchungsdienst. */
    BookingIf bookingIf;

    /**
     * Initialisiert das Servlet und stellt die Verbindung zum Buchungsdienst über RMI her.
     *
     * @throws ServletException Wenn die Verbindung zum RMI-Server fehlschlägt.
     */
    @Override
    public void init() throws ServletException {
        try {
            bookingIf = (BookingIf) Naming.lookup("rmi://localhost:1099/BookingIf");
        } catch (Exception e) {
            throw new ServletException("Failed to lookup RMI server", e);
        }
    }

    /**
     * Verarbeitet GET-Anfragen, um die Nutzungshistorie eines Benutzers abzurufen und als JSON-Antwort zurückzugeben.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        System.out.println("Fetching usage history for user: " + username); // Debugging Log

        JSONArray usageHistory = new JSONArray();
        try {
            // Abrufen der Nutzungshistorie vom Buchungsdienst
            List<Map<String, Object>> resultSet = bookingIf.getUsageHistory(username);
            for (Map<String, Object> result : resultSet) {
                JSONObject usage = new JSONObject();
                usage.put("vehicleId", result.get("vehicleId"));
                usage.put("startTime", result.get("startTime"));
                usage.put("endTime", result.get("endTime"));
                usage.put("make", result.get("make"));
                usage.put("model", result.get("model"));
                usage.put("year", result.get("year"));
                usageHistory.put(usage); // Hinzufügen des Nutzungseintrags zur JSON-Antwort
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Usage history JSON: " + usageHistory.toString()); // Debug-Ausgabe

        response.setContentType("application/json");
        response.getWriter().write(usageHistory.toString());
    }

    /**
     * Verarbeitet POST-Anfragen, um einen neuen Nutzungseintrag für ein Fahrzeug zu speichern.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        int vehicleId = Integer.parseInt(request.getParameter("vehicleId"));
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        boolean result = false;
        try {
            // Aufzeichnung der Fahrzeugnutzung über den Buchungsdienst
            result = bookingIf.recordUsage(username, vehicleId, startTime, endTime);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", result);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
