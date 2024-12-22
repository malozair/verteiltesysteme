package com.example.carconnect.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket-Endpunkt zur Verwaltung von Benachrichtigungen. Dieser Endpunkt ermöglicht es dem Server,
 * Benachrichtigungen an alle verbundenen Clients zu senden.
 *
 * @autor Mohammed Al-Ozair
 * @autor Nabeel Elamaireh
 */
@ServerEndpoint("/notifications")
public class NotificationEndpoint {

    /** Set zur Verwaltung aller aktiven Verbindungen zu diesem WebSocket-Endpunkt. */
    private static final Set<NotificationEndpoint> connections = new CopyOnWriteArraySet<>();

    /** Die WebSocket-Session, die mit dem Client verbunden ist. */
    private Session session;

    /**
     * Methode, die aufgerufen wird, wenn eine neue WebSocket-Verbindung geöffnet wird.
     *
     * @param session Die Session, die die Verbindung zum Client repräsentiert.
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connections.add(this); // Fügt die neue Verbindung zur Liste der aktiven Verbindungen hinzu
    }

    /**
     * Methode, die aufgerufen wird, wenn eine Nachricht vom Client empfangen wird.
     *
     * @param message Die vom Client empfangene Nachricht.
     * @param session Die Session, die die Verbindung zum Client repräsentiert.
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received message from session id: " + session.getId() + ": " + message);
    }

    /**
     * Methode, die aufgerufen wird, wenn die WebSocket-Verbindung geschlossen wird.
     *
     * @param session Die Session, die die Verbindung zum Client repräsentiert.
     */
    @OnClose
    public void onClose(Session session) {
        connections.remove(this); // Entfernt die Verbindung aus der Liste der aktiven Verbindungen
    }

    /**
     * Methode, die aufgerufen wird, wenn ein Fehler in der WebSocket-Verbindung auftritt.
     *
     * @param session Die Session, die die Verbindung zum Client repräsentiert.
     * @param throwable Das aufgetretene Fehlerobjekt.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on session id: " + session.getId() + ": " + throwable.getMessage());
    }

    /**
     * Sendet eine Benachrichtigung an alle verbundenen Clients.
     *
     * @param message Die Nachricht, die an alle Clients gesendet werden soll.
     */
    public static void sendNotification(String message) {
        for (NotificationEndpoint endpoint : connections) {
            synchronized (endpoint) { // Synchronisiert den Zugriff auf die Session, um Thread-Sicherheit zu gewährleisten
                try {
                    endpoint.session.getBasicRemote().sendText(message); // Sendet die Nachricht an den Client
                    System.out.println("Sent message to session id: " + endpoint.session.getId() + ": " + message);
                } catch (IOException e) {
                    System.err.println("Failed to send message to session id: " + endpoint.session.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
