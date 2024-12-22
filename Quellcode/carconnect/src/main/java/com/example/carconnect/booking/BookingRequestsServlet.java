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
 * Servlet zur Verwaltung von Buchungsanfragen eines bestimmten Benutzers.
 * Dieses Servlet kommuniziert über RMI mit dem Buchungsdienst, um die Buchungsanfragen abzurufen und als JSON zurückzugeben.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "BookingRequestsServlet", urlPatterns = {"/bookingRequests"})
public class BookingRequestsServlet extends HttpServlet {

    /** Remote-Referenz auf den Buchungsdienst. */
    private BookingIf bookingIf;

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
     * Verarbeitet GET-Anfragen, um die Buchungsanfragen eines bestimmten Benutzers abzurufen und als JSON-Antwort zurückzugeben.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        System.out.println("Received username: " + username); // Debugging Log
        JSONArray bookingRequests = new JSONArray();
        try {
            // Abrufen der Buchungsanfragen vom Buchungsdienst
            List<Map<String, Object>> resultSet = bookingIf.getBookingRequests(username);
            for (Map<String, Object> result : resultSet) {
                JSONObject requestJson = new JSONObject();
                requestJson.put("id", result.get("id"));
                requestJson.put("vehicle", result.get("vehicle"));
                requestJson.put("requester", result.get("requester"));
                requestJson.put("start_time", result.get("start_time"));
                requestJson.put("end_time", result.get("end_time"));
                requestJson.put("status", result.get("status"));
                bookingRequests.put(requestJson); // Hinzufügen der Buchungsanfrage zur JSON-Antwort
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Booking Requests: " + bookingRequests.toString()); // Debug-Ausgabe
        response.setContentType("application/json");
        response.getWriter().write(bookingRequests.toString());
    }
}
