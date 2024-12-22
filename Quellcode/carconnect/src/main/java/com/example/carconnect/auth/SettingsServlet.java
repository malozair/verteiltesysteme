package com.example.carconnect.auth;

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
 * Servlet zur Verwaltung von Benutzereinstellungen, speziell zum Ändern des Passworts.
 * Dieses Servlet kommuniziert über RMI mit dem Authentifizierungsdienst, um Passwortänderungen durchzuführen.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "SettingsServlet", urlPatterns = {"/changePassword"})
public class SettingsServlet extends HttpServlet {

    /** Remote-Referenz auf den Authentifizierungsdienst. */
    private AuthIf authIf;

    /**
     * Initialisiert das Servlet und stellt die Verbindung zum Authentifizierungsdienst über RMI her.
     *
     * @throws ServletException Wenn die Verbindung zum RMI-Server fehlschlägt.
     */
    @Override
    public void init() throws ServletException {
        try {
            authIf = (AuthIf) Naming.lookup("rmi://localhost:1099/AuthIf");
        } catch (Exception e) {
            throw new ServletException("Failed to lookup RMI server", e);
        }
    }

    /**
     * Verarbeitet POST-Anfragen, um das Passwort eines Benutzers zu ändern.
     *
     * @param request  Das HttpServletRequest-Objekt, das die Anfrage vom Client enthält.
     * @param response Das HttpServletResponse-Objekt, das die Antwort an den Client sendet.
     * @throws ServletException Wenn eine Servlet-spezifische Ausnahme auftritt.
     * @throws IOException Wenn ein Ein-/Ausgabefehler auftritt.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        JSONObject jsonResponse = new JSONObject();

        try {
            // Passwortänderung über den Authentifizierungsdienst
            boolean result = authIf.changePassword(username, oldPassword, newPassword);
            jsonResponse.put("success", result);
            if (!result) {
                jsonResponse.put("message", "Password change failed. Old password may be incorrect.");
            }
        } catch (RemoteException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
