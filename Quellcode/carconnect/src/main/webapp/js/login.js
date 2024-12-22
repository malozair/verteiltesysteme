// login.js
document.addEventListener("DOMContentLoaded", function() {
    // Einrichtung der WebSocket-Verbindung für Benachrichtigungen
    const socket = new WebSocket("ws://localhost:8080/carconnect_war_exploded/notifications");

    // Event-Handler für die erfolgreiche WebSocket-Verbindung
    socket.onopen = function() {
        console.log("WebSocket connection established");
    };

    // Event-Handler für empfangene WebSocket-Nachrichten
    socket.onmessage = function(event) {
        console.log("WebSocket message received:", event.data);
        // Ruft alle Fahrzeuge, Nutzungsverlauf und Buchungsanfragen ab, wenn eine Nachricht empfangen wird
        fetchAllVehicles();
        fetchUsageHistory();
        fetchBookingRequests();
    };

    // Event-Handler für das Schließen der WebSocket-Verbindung
    socket.onclose = function() {
        console.log("WebSocket connection closed");
    };

    // Event-Handler für WebSocket-Fehler
    socket.onerror = function(error) {
        console.error("WebSocket error:", error);
    };

    // Referenzen zu den Formularen auf der Login-Seite
    const registerForm = document.getElementById("registerForm");
    const loginForm = document.getElementById("loginForm");

    // Authentifizierungstoken aus dem lokalen Speicher abrufen
    let authToken = localStorage.getItem('token');

    // Event-Listener für das Registrierungsformular
    if (registerForm) {
        registerForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Verhindert das Standard-Formularverhalten (Seitenreload)
            const username = document.getElementById("registerUsername").value;
            const password = document.getElementById("registerPassword").value;

            // Senden der Registrierungsdaten an den Server
            fetch("http://localhost:8080/carconnect_war_exploded/auth", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: `action=register&username=${username}&password=${password}`
            })
                .then(response => response.json())
                .then(data => {
                    alert("Registration " + (data.success ? "successful" : "failed"));
                });
        });
    }

    // Event-Listener für das Login-Formular
    if (loginForm) {
        loginForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Verhindert das Standard-Formularverhalten (Seitenreload)
            const username = document.getElementById("loginUsername").value;
            const password = document.getElementById("loginPassword").value;

            // Senden der Login-Daten an den Server
            fetch("http://localhost:8080/carconnect_war_exploded/auth", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: `action=login&username=${username}&password=${password}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Speichern des Tokens und Benutzernamens im lokalen Speicher bei erfolgreichem Login
                        authToken = data.token;
                        localStorage.setItem('token', authToken);
                        localStorage.setItem('username', username);
                        alert("Login successful");
                        window.location.href = "landing.html"; // Weiterleiten zur Landing-Page
                    } else {
                        alert("Login failed");
                    }
                });
        });
    }
});
