// settings.js
document.addEventListener("DOMContentLoaded", function() {
    // Authentifizierungstoken und Benutzername aus dem lokalen Speicher abrufen
    const authToken = localStorage.getItem('token');
    const username = localStorage.getItem('username');

    // Überprüfung, ob das Authentifizierungstoken und der Benutzername vorhanden sind
    if (!authToken || !username) {
        window.location.href = 'login.html'; // Weiterleitung zur Login-Seite, falls nicht angemeldet
    }

    // Anzeigen des Benutzernamens auf der Seite
    document.getElementById('usernameDisplay').innerText = username;

    // Event-Listener für das Formular zum Ändern des Passworts
    document.getElementById('changePasswordForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Verhindert das Standard-Formularverhalten (Seitenreload)

        // Abrufen der eingegebenen Passwörter
        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmNewPassword = document.getElementById('confirmNewPassword').value;

        // Überprüfung, ob das neue Passwort mit der Bestätigung übereinstimmt
        if (newPassword !== confirmNewPassword) {
            alert('New password and confirmation do not match.');
            return;
        }

        // Senden der Passwortänderungsanfrage an den Server
        fetch("http://localhost:8080/carconnect_war_exploded/changePassword", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Authorization": `Bearer ${authToken}`
            },
            body: `username=${username}&oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Password changed successfully.');
                    document.getElementById('changePasswordForm').reset(); // Zurücksetzen des Formulars
                } else {
                    alert('Password change failed: ' + (data.message || 'Unknown error'));
                }
            })
            .catch(error => console.error('Error changing password:', error)); // Fehlerbehandlung
    });

    // Event-Listener für den Logout-Button
    document.getElementById('logoutButton').addEventListener('click', function() {
        // Entfernen des Tokens und Benutzernamens aus dem lokalen Speicher und Weiterleitung zur Login-Seite
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        window.location.href = 'login.html';
    });
});
