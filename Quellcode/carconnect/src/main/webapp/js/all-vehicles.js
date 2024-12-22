// add-vehicle.js
document.addEventListener("DOMContentLoaded", function () {
    // Token und Benutzername werden aus dem lokalen Speicher abgerufen
    let authToken = localStorage.getItem('token');
    let username = localStorage.getItem('username');

    // Überprüfung, ob Authentifizierungstoken und Benutzername vorhanden sind
    if (!authToken || !username) {
        console.error("Auth token or username is not defined. Please login again.");
        return;
    }

    // WebSocket-Verbindung zu Benachrichtigungen herstellen
    const socket = new WebSocket("ws://localhost:8080/carconnect_war_exploded/notifications");

    // Event-Handler für die Eröffnung der WebSocket-Verbindung
    socket.onopen = function () {
        console.log("WebSocket connection established");
    };

    // Event-Handler für empfangene WebSocket-Nachrichten
    socket.onmessage = function (event) {
        console.log("WebSocket message received:", event.data);
        const data = JSON.parse(event.data);

        // Verarbeitung der empfangenen Nachricht basierend auf der Aktion
        if (data.action === 'newVehicle') {
            addVehicleToTable(data.vehicle);
        } else if (data.action === 'approveBookingRequest') {
            removeVehicleFromTable(data.vehicleId);
        } else if (data.action === "update") {
            updateVehicleRow(data);
        } else if (data.action === "delete") {
            deleteVehicleRow(data.vehicleId);
        } else if (data.action === "add") {
            addVehicleRow(data);
        }
    };

    // Event-Handler für das Schließen der WebSocket-Verbindung
    socket.onclose = function () {
        console.log("WebSocket connection closed");
    };

    // Event-Handler für WebSocket-Fehler
    socket.onerror = function (error) {
        console.error("WebSocket error:", error);
    };

    // Formular für Fahrzeugbuchungen
    const bookVehicleForm = document.getElementById("bookVehicleForm");

    // Event-Delegation für Klickereignisse
    document.addEventListener('click', function (event) {
        // Buchungsbutton für ein Fahrzeug
        if (event.target.classList.contains('book-vehicle')) {
            const vehicleId = event.target.getAttribute('data-id');
            bookVehicleForm.setAttribute('data-id', vehicleId);
            $('#bookVehicle-tab').tab('show'); // Wechsel zur Buchungsansicht
        }

        // Bearbeitungsbutton für ein Fahrzeug
        if (event.target.classList.contains('edit-vehicle')) {
            const row = event.target.closest('tr');
            Array.from(row.querySelectorAll('[data-type]')).forEach(cell => {
                cell.setAttribute('contenteditable', 'true');
                cell.classList.add('editable');
            });
            row.querySelector('.save-vehicle').classList.remove('d-none');
            event.target.classList.add('d-none');
        }

        // Speichern der Änderungen für ein Fahrzeug
        if (event.target.classList.contains('save-vehicle')) {
            const row = event.target.closest('tr');
            const vehicleId = row.getAttribute('data-id');
            const make = row.querySelector('[data-type="make"]').innerText;
            const model = row.querySelector('[data-type="model"]').innerText;
            const year = row.querySelector('[data-type="year"]').innerText;
            const location = row.querySelector('[data-type="location"]').innerText;

            // Senden der aktualisierten Fahrzeugdaten an den Server
            fetch("http://localhost:8080/carconnect_war_exploded/vehicles", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Authorization": `Bearer ${authToken}`
                },
                body: `username=${username}&action=update&vehicleId=${vehicleId}&make=${make}&model=${model}&year=${year}&location=${location}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        Array.from(row.querySelectorAll('[data-type]')).forEach(cell => {
                            cell.setAttribute('contenteditable', 'false');
                            cell.classList.remove('editable');
                        });
                        row.querySelector('.edit-vehicle').classList.remove('d-none');
                        event.target.classList.add('d-none');
                    } else {
                        alert("Vehicle update failed");
                    }
                })
                .catch(error => console.error('Error updating vehicle:', error));
        }

        // Löschbutton für ein Fahrzeug
        if (event.target.classList.contains('delete-vehicle')) {
            const vehicleId = event.target.getAttribute('data-id');
            // Senden der Löschanfrage für das Fahrzeug an den Server
            fetch("http://localhost:8080/carconnect_war_exploded/vehicles", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Authorization": `Bearer ${authToken}`
                },
                body: `username=${username}&action=delete&vehicleId=${vehicleId}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        event.target.closest('tr').remove();
                    } else {
                        alert("Vehicle deletion failed");
                    }
                })
                .catch(error => console.error('Error deleting vehicle:', error));
        }
    });

    // Verarbeitung des Buchungsformulars
    if (bookVehicleForm) {
        bookVehicleForm.addEventListener("submit", function (event) {
            event.preventDefault();
            const vehicleId = bookVehicleForm.getAttribute('data-id');
            const startTime = new Date(document.getElementById("startTime").value).toISOString().slice(0, 19).replace('T', ' ');
            const endTime = new Date(document.getElementById("endTime").value).toISOString().slice(0, 19).replace('T', ' ');

            // Senden der Buchungsanfrage an den Server
            fetch("http://localhost:8080/carconnect_war_exploded/bookVehicle", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Authorization": `Bearer ${authToken}`
                },
                body: `action=book&username=${username}&vehicleId=${vehicleId}&startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert("Vehicle booking successful");
                    } else {
                        alert("Vehicle booking failed: " + (data.message || "Unknown error"));
                    }
                })
                .catch(error => console.error("Error booking vehicle:", error));
        });
    }

    // Funktion zum Abrufen aller Fahrzeuge
    function fetchAllVehicles() {
        fetch("http://localhost:8080/carconnect_war_exploded/vehicles", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${authToken}`
            }
        })
            .then(response => {
                if (!response.ok) { // Prüfe, ob der Statuscode 2xx ist
                    if (response.status === 404) {
                        throw new Error("Resource not found");
                    } else if (response.status === 500) {
                        throw new Error("Internal Server Error");
                    }
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json(); // Versuche, JSON zu parsen
            })
            .then(data => {
                populateVehiclesTable(data.vehicles);
            })
            .catch(error => console.error('Error fetching vehicles:', error));
    }

    // Funktion zum Suchen von Fahrzeugen
    function searchVehicles() {
        const make = document.getElementById("searchMake").value;
        const model = document.getElementById("searchModel").value;
        const year = document.getElementById("searchYear").value;

        fetch("http://localhost:8080/carconnect_war_exploded/searchVehicles", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Authorization": `Bearer ${authToken}`
            },
            body: `make=${make}&model=${model}&year=${year}`
        })
            .then(response => response.json())
            .then(data => {
                populateVehiclesTable(data);
            })
            .catch(error => console.error('Error searching vehicles:', error));
    }

    // Funktion zum Befüllen der Fahrzeugtabelle
    function populateVehiclesTable(vehicles) {
        const tbody = document.getElementById("allVehiclesTableBody");
        tbody.innerHTML = "";
        if (vehicles && vehicles.length > 0) {
            vehicles.forEach(vehicle => {
                const row = document.createElement("tr");
                let actions = `<button class="btn btn-primary book-vehicle" data-id="${vehicle.id}">Book</button>`;
                if (vehicle.ownerName === username) {
                    actions += `
                        <button class="btn btn-secondary edit-vehicle" data-id="${vehicle.id}">Edit</button>
                        <button class="btn btn-success save-vehicle d-none" data-id="${vehicle.id}">Save</button>
                        <button class="btn btn-danger delete-vehicle" data-id="${vehicle.id}">Delete</button>`;
                }
                row.innerHTML = `<td data-type="make" contenteditable="false">${vehicle.make}</td>
                                 <td data-type="model" contenteditable="false">${vehicle.model}</td>
                                 <td data-type="year" contenteditable="false">${vehicle.year}</td>
                                 <td data-type="location" contenteditable="false">${vehicle.location}</td>
                                 <td>${actions}</td>`;
                row.setAttribute("data-id", vehicle.id);
                tbody.appendChild(row);
            });
        } else {
            tbody.innerHTML = "<tr><td colspan='5' class='text-center'>No vehicles found</td></tr>";
        }
    }

    // Funktion zum Hinzufügen einer Fahrzeugzeile zur Tabelle
    function addVehicleRow(vehicle) {
        const tbody = document.getElementById("allVehiclesTableBody");
        const row = document.createElement("tr");
        let actions = `<button class="btn btn-primary book-vehicle" data-id="${vehicle.vehicleId}">Book</button>`;
        if (vehicle.ownerName === username) {
            actions += `
                <button class="btn btn-secondary edit-vehicle" data-id="${vehicle.vehicleId}">Edit</button>
                <button class="btn btn-success save-vehicle d-none" data-id="${vehicle.vehicleId}">Save</button>
                <button class="btn btn-danger delete-vehicle" data-id="${vehicle.vehicleId}">Delete</button>`;
        }
        row.innerHTML = `<td data-type="make" contenteditable="false">${vehicle.make}</td>
                         <td data-type="model" contenteditable="false">${vehicle.model}</td>
                         <td data-type="year" contenteditable="false">${vehicle.year}</td>
                         <td data-type="location" contenteditable="false">${vehicle.location}</td>
                         <td>${actions}</td>`;
        row.setAttribute("data-id", vehicle.vehicleId);
        tbody.appendChild(row);
    }

    // Funktion zum Aktualisieren einer Fahrzeugzeile in der Tabelle
    function updateVehicleRow(vehicle) {
        const row = document.querySelector(`tr[data-id='${vehicle.vehicleId}']`);
        if (row) {
            row.querySelector('[data-type="make"]').innerText = vehicle.make;
            row.querySelector('[data-type="model"]').innerText = vehicle.model;
            row.querySelector('[data-type="year"]').innerText = vehicle.year;
            row.querySelector('[data-type="location"]').innerText = vehicle.location;
        }
    }

    // Funktion zum Löschen einer Fahrzeugzeile aus der Tabelle
    function deleteVehicleRow(vehicleId) {
        const row = document.querySelector(`tr[data-id='${vehicleId}']`);
        if (row) {
            row.remove();
        }
    }

    // Funktion zum Hinzufügen eines Fahrzeugs zur Tabelle (via WebSocket)
    function addVehicleToTable(vehicle) {
        const tbody = document.getElementById("allVehiclesTableBody");
        const row = document.createElement("tr");
        let actions = `<button class="btn btn-primary book-vehicle" data-id="${vehicle.id}">Book</button>`;
        if (vehicle.ownerName === username) {
            actions += `
                <button class="btn btn-secondary edit-vehicle" data-id="${vehicle.id}">Edit</button>
                <button class="btn btn-success save-vehicle d-none" data-id="${vehicle.id}">Save</button>
                <button class="btn btn-danger delete-vehicle" data-id="${vehicle.id}">Delete</button>`;
        }
        row.innerHTML = `<td data-type="make" contenteditable="false">${vehicle.make}</td>
                         <td data-type="model" contenteditable="false">${vehicle.model}</td>
                         <td data-type="year" contenteditable="false">${vehicle.year}</td>
                         <td data-type="location" contenteditable="false">${vehicle.location}</td>
                         <td>${actions}</td>`;
        row.setAttribute("data-id", vehicle.id);
        tbody.appendChild(row);
    }

    // Funktion zum Entfernen eines Fahrzeugs aus der Tabelle
    function removeVehicleFromTable(vehicleId) {
        const tbody = document.getElementById("allVehiclesTableBody");
        const row = tbody.querySelector(`tr[data-id="${vehicleId}"]`);
        if (row) {
            row.remove();
        }
    }

    // Event-Listener für die Suchfunktion
    document.getElementById("searchButton").addEventListener("click", function () {
        searchVehicles();
    });

    // Initiales Abrufen aller Fahrzeuge, wenn die Seite geladen wird
    if (document.getElementById("allVehicles")) {
        fetchAllVehicles();
    }
});
