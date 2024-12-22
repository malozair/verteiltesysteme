package com.example.carconnect.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Servlet zur Verwaltung von Authentifizierungsoperationen wie Registrierung und Anmeldung.
 * Dieses Servlet kommuniziert über RMI mit einem entfernten Authentifizierungsdienst und verwendet JWT zur Token-Generierung.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth"})
public class AuthServlet extends HttpServlet {

    /** Remote-Referenz auf den Authentifizierungsdienst. */
    private AuthIf authIf;

    /** Geheimschlüssel für die JWT-Token-Generierung. */
    private static final String SECRET_KEY = "12345";

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
     * Verarbeitet POST-Anfragen für die Authentifizierung, einschließlich Registrierung und Anmeldung.
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
            switch (action) {
                case "register":
                    // Verarbeitung der Benutzerregistrierung
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    boolean registerResult = authIf.registerUser(username, hashPassword(password));
                    jsonResponse.put("success", registerResult);
                    break;
                case "login":
                    // Verarbeitung der Benutzeranmeldung
                    username = request.getParameter("username");
                    password = request.getParameter("password");
                    long sessionId = authIf.generateNewSessionId(username);
                    boolean validateResult = authIf.validateUser(sessionId, hashSessionId(sessionId, hashPassword(password)));
                    if (validateResult) {
                        String token = generateToken(username);
                        jsonResponse.put("success", true);
                        jsonResponse.put("token", token);
                    } else {
                        jsonResponse.put("success", false);
                    }
                    break;
                default:
                    jsonResponse.put("error", "Invalid action");
            }
        } catch (RemoteException | NoSuchAlgorithmException e) {
            jsonResponse.put("error", e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }

    /**
     * Hashes das Passwort mit dem SHA-256-Algorithmus.
     *
     * @param password Das zu hashende Passwort.
     * @return Der resultierende Passwort-Hash als hexadezimale Zeichenkette.
     * @throws NoSuchAlgorithmException Wenn der SHA-256-Algorithmus nicht verfügbar ist.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(password.getBytes());
        return bytesToHex(encodedHash);
    }

    /**
     * Hashes die Sitzungs-ID zusammen mit dem Passwort-Hash mit dem SHA-256-Algorithmus.
     *
     * @param sessionId    Die Sitzungs-ID.
     * @param passwordHash Der Passwort-Hash.
     * @return Der resultierende Hash als hexadezimale Zeichenkette.
     * @throws NoSuchAlgorithmException Wenn der SHA-256-Algorithmus nicht verfügbar ist.
     */
    private String hashSessionId(long sessionId, String passwordHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String input = sessionId + passwordHash;
        byte[] encodedHash = digest.digest(input.getBytes());
        return bytesToHex(encodedHash);
    }

    /**
     * Wandelt ein Byte-Array in eine hexadezimale Zeichenkette um.
     *
     * @param hash Das zu konvertierende Byte-Array.
     * @return Die hexadezimale Darstellung des Byte-Arrays.
     */
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Generiert ein JWT-Token für den angegebenen Benutzernamen.
     *
     * @param username Der Benutzername, für den das Token generiert wird.
     * @return Das generierte JWT-Token als Zeichenkette.
     */
    private String generateToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }
}
