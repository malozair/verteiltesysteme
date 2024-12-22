package com.example.carconnect.auth;

import com.example.carconnect.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementierung der Authentifizierungs-Logik, die die AuthIf-Schnittstelle über RMI bereitstellt.
 * Diese Klasse bietet Methoden zur Sitzungsverwaltung, Benutzervalidierung, Registrierung und Passwortänderung.
 *
 * @author Mohammed Al-Ozair
 * @author Nabeel Elamaireh
 */
public class AuthImpl extends UnicastRemoteObject implements AuthIf {

    /** Repository für Benutzerinformationen. */
    private final UserRepository userRepository;

    /** Map, die Sitzungs-IDs den Benutzernamen zuordnet. */
    private Map<Long, String> sessionToUserMap = new HashMap<>();

    /**
     * Konstruktor für AuthImpl.
     *
     * @param userRepository Das Repository zur Verwaltung von Benutzerinformationen.
     * @throws RemoteException Wenn ein Remote-Methodenaufruf fehlschlägt.
     */
    public AuthImpl(UserRepository userRepository) throws RemoteException {
        super();
        this.userRepository = userRepository;
    }

    /**
     * Generiert eine neue Sitzungs-ID für einen Benutzer. Wenn der Benutzer bereits eine aktive Sitzung hat,
     * wird die existierende Sitzungs-ID zurückgegeben.
     *
     * @param username Der Benutzername, für den eine neue Sitzungs-ID erstellt werden soll.
     * @return Die neue oder bestehende Sitzungs-ID.
     */
    @Override
    public long generateNewSessionId(String username) {
        // Überprüfung, ob eine Sitzung bereits existiert
        for (Map.Entry<Long, String> entry : sessionToUserMap.entrySet()) {
            if (entry.getValue().equals(username)) {
                return entry.getKey();  // Rückgabe der existierenden Sitzungs-ID
            }
        }
        long newSessionId = new Date().getTime();  // Neue Sitzungs-ID basierend auf der aktuellen Zeit
        sessionToUserMap.put(newSessionId, username);  // Speichern der neuen Sitzungs-ID und des Benutzernamens
        return newSessionId;
    }

    /**
     * Validiert einen Benutzer anhand der Sitzungs-ID und eines Hashes.
     *
     * @param sessionId Die Sitzungs-ID, die überprüft werden soll.
     * @param hash Der übermittelte Hash zur Validierung.
     * @return true, wenn der Benutzer validiert werden konnte, false andernfalls.
     */
    @Override
    public boolean validateUser(long sessionId, String hash) {
        String username = sessionToUserMap.get(sessionId);  // Abrufen des Benutzernamens anhand der Sitzungs-ID
        System.out.println("Validating user: " + username);
        if (username == null) {
            System.out.println("Username not found for session ID: " + sessionId);
            return false;  // Abbruch, wenn der Benutzername nicht gefunden wird
        }
        if (!userRepository.checkUserExistence(username)) {
            System.out.println("User does not exist: " + username);
            return false;  // Abbruch, wenn der Benutzer nicht existiert
        }
        String userHash = userRepository.getHash(username);  // Abrufen des gespeicherten Hashes
        if (userHash == null) {
            System.out.println("Hash not found for user: " + username);
            return false;  // Abbruch, wenn kein Hash gefunden wird
        }
        try {
            // Erstellen eines neuen Hashes basierend auf der Sitzungs-ID und dem Benutzerhash
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digester.digest((sessionId + userHash).getBytes(StandardCharsets.UTF_8));
            String newHash = bytesToHex(encodedHash);  // Umwandeln des Hashes in Hexadezimal-Format
            sessionToUserMap.remove(sessionId);  // Entfernen der Sitzungs-ID nach Validierung
            System.out.println("Expected hash: " + newHash + ", Received hash: " + hash);
            return newHash.equals(hash);  // Vergleich des neuen Hashes mit dem übermittelten Hash
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;  // Rückgabe von false im Fehlerfall
    }

    /**
     * Registriert einen neuen Benutzer mit einem Benutzernamen und einem Passwort-Hash.
     *
     * @param username Der Benutzername des neuen Benutzers.
     * @param hash Der Hash des Passworts für den neuen Benutzer.
     * @return true, wenn die Registrierung erfolgreich war, false andernfalls.
     */
    @Override
    public boolean registerUser(String username, String hash) {
        System.out.println("Registering user: " + username);
        boolean userExists = userRepository.checkUserExistence(username);  // Überprüfen, ob der Benutzer bereits existiert
        System.out.println("User exists: " + userExists);
        if (userExists) {
            return false;  // Abbruch, wenn der Benutzer bereits existiert
        }
        boolean registrationResult = userRepository.registerNewUser(username, hash);  // Registrierung des neuen Benutzers
        System.out.println("Registration result: " + registrationResult);
        return registrationResult;
    }

    /**
     * Ändert das Passwort eines bestehenden Benutzers.
     *
     * @param username Der Benutzername des Benutzers, dessen Passwort geändert werden soll.
     * @param oldPassword Das alte Passwort des Benutzers.
     * @param newPassword Das neue Passwort, das gesetzt werden soll.
     * @return true, wenn das Passwort erfolgreich geändert wurde, false andernfalls.
     */
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            String currentHash = userRepository.getHash(username);  // Abrufen des aktuellen Passwort-Hashes
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            byte[] oldPasswordHash = digester.digest(oldPassword.getBytes());  // Hashen des alten Passworts
            if (!currentHash.equals(bytesToHex(oldPasswordHash))) {
                return false;  // Abbruch, wenn der alte Passwort-Hash nicht übereinstimmt
            }
            byte[] newPasswordHash = digester.digest(newPassword.getBytes());  // Hashen des neuen Passworts
            return userRepository.updateUserPassword(username, bytesToHex(newPasswordHash));  // Aktualisieren des Passworts
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;  // Rückgabe von false im Fehlerfall
        }
    }

    /**
     * Hilfsmethode, die ein Byte-Array in eine hexadezimale Zeichenkette umwandelt.
     *
     * @param hash Das Byte-Array, das umgewandelt werden soll.
     * @return Eine hexadezimale Darstellung des Byte-Arrays.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);  // Umwandlung jedes Bytes in einen hexadezimalen Wert
            if (hex.length() == 1) {
                hexString.append('0');  // Hinzufügen einer führenden Null, falls nötig
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
