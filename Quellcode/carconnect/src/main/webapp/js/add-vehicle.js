// add-vehicle.js
// Event Listener, der auf das Laden des DOMs wartet, um sicherzustellen, dass das Skript nach dem Laden des gesamten HTML-Dokuments ausgeführt wird
document.addEventListener("DOMContentLoaded", function() {
    // Referenz auf das Fahrzeugregistrierungsformular im DOM
    const registerVehicleForm = document.getElementById("registerVehicleForm");

    // Abrufen des Authentifizierungstokens und des Benutzernamens aus dem lokalen Speicher
    let authToken = localStorage.getItem('token');
    let username = localStorage.getItem('username');

    // Überprüfen, ob das Fahrzeugregistrierungsformular im DOM vorhanden ist
    if (registerVehicleForm) {
        // Hinzufügen eines Event Listeners, der beim Absenden des Formulars ausgelöst wird
        registerVehicleForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Verhindert das Standardverhalten des Formulars (Seitenneuladen)

            // Abrufen der Werte aus den Formularfeldern
            const make = document.getElementById("make").value;
            const model = document.getElementById("model").value;
            const year = document.getElementById("year").value;
            const location = document.getElementById("location").value;

            // Senden der Fahrzeugdaten an den Server über eine POST-Anfrage
            fetch("http://localhost:8080/carconnect_war_exploded/registerVehicle", {
                method: "POST", // HTTP-Methode, die verwendet wird, um Daten an den Server zu senden
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded", // Setzen des Inhalts-Typs der Anfrage
                    "Authorization": `Bearer ${authToken}` // Hinzufügen des Authentifizierungstokens im Header
                },
                // Erstellen des Anfragekörpers mit den Fahrzeugdaten
                body: `ownerUsername=${username}&make=${make}&model=${model}&year=${year}&location=${location}`
            })
                // Verarbeiten der Serverantwort
                .then(response => response.json()) // Konvertieren der Antwort in ein JSON-Objekt
                .then(data => {
                    // Anzeigen einer Benachrichtigung über den Erfolg oder das Scheitern der Fahrzeugregistrierung
                    alert("Vehicle registration " + (data.success ? "successful" : "failed"));
                });
        });
    }
});
