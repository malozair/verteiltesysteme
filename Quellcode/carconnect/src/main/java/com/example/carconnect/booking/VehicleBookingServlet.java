package com.example.carconnect.booking;

import com.example.carconnect.websocket.NotificationEndpoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Servlet zur Verwaltung von Fahrzeugbuchungen. Es ermöglicht das Erstellen, Genehmigen und Ablehnen von Buchungsanfragen
 * und sendet entsprechende Benachrichtigungen über Websockets.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "VehicleBookingServlet", urlPatterns = {"/bookVehicle"})
public class VehicleBookingServlet extends HttpServlet {

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
     * Verarbeitet POST-Anfragen zur Verwaltung von Buchungen, einschließlich Buchungsanfragen, Genehmigungen und Ablehnungen.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        JSONObject jsonResponse = new JSONObject();

        try {
            if ("book".equals(action)) {
                // Verarbeitung einer neuen Buchungsanfrage
                String username = request.getParameter("username");
                int vehicleId = Integer.parseInt(request.getParameter("vehicleId"));
                String startTime = request.getParameter("startTime");
                String endTime = request.getParameter("endTime");

                boolean result = bookingIf.insertBookingRequest(username, vehicleId, startTime, endTime);
                jsonResponse.put("success", result);
                if (result) {
                    // Senden einer Benachrichtigung über eine neue Buchungsanfrage
                    JSONObject notification = new JSONObject();
                    notification.put("action", "newBookingRequest");
                    notification.put("username", username);
                    notification.put("vehicleId", vehicleId);
                    notification.put("startTime", startTime);
                    notification.put("endTime", endTime);
                    NotificationEndpoint.sendNotification(notification.toString());
                } else {
                    jsonResponse.put("message", "Booking request failed due to an unknown reason.");
                }
            } else if ("approve".equals(action)) {
                // Verarbeitung der Genehmigung einer Buchungsanfrage
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                boolean result = bookingIf.approveBookingRequest(requestId);
                jsonResponse.put("success", result);
                if (result) {
                    int vehicleId = bookingIf.getVehicleIdFromBookingRequest(requestId);
                    // Senden einer Benachrichtigung über die Genehmigung der Buchungsanfrage
                    JSONObject notification = new JSONObject();
                    notification.put("action", "approveBookingRequest");
                    notification.put("vehicleId", vehicleId);
                    NotificationEndpoint.sendNotification(notification.toString());
                } else {
                    jsonResponse.put("message", "Approval failed due to an unknown reason.");
                }
            } else if ("reject".equals(action)) {
                // Verarbeitung der Ablehnung einer Buchungsanfrage
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                boolean result = bookingIf.rejectBookingRequest(requestId);
                jsonResponse.put("success", result);
            } else {
                jsonResponse.put("error", "Invalid action");
            }
        } catch (RemoteException e) {
            jsonResponse.put("error", e.getMessage());
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
