# 📱 Anleitung: Veloce App auf dem Android-Handy installieren

Diese Anleitung führt dich Schritt für Schritt durch das Erstellen und Installieren der Veloce Kotlin Multiplatform App auf deinem Android-Smartphone.

---

## 🛠️ Voraussetzungen
1. **Android Studio** (empfohlen) oder **Java 17+ & Android SDK** auf deinem PC/Laptop installiert.
2. Dein **Android-Smartphone** mit einem USB-Kabel am PC angeschlossen.

---

## 🚀 Option 1: Direkt über Android Studio (Empfohlen & am einfachsten)

1. **Projekt in Android Studio öffnen**:
   - Starte Android Studio.
   - Klicke auf **Open** und wähle den Ordner `c:\repos\Veloce\VeloceApp` aus.
   - Warte, bis der **Gradle Sync** abgeschlossen ist.

2. **Entwickleroptionen auf dem Handy aktivieren**:
   - Gehe auf deinem Handy in **Einstellungen** ➔ **Über das Telefon**.
   - Tippe 7-mal hintereinander auf **Build-Nummer**, bis die Meldung *"Du bist jetzt Entwickler!"* erscheint.
   - Gehe zurück in **Einstellungen** ➔ **System** ➔ **Entwickleroptionen** und aktiviere **USB-Debugging**.

3. **Handy per USB verbinden**:
   - Schließe dein Handy an den PC an.
   - Auf dem Handy-Bildschirm erscheint eine Abfrage: *"USB-Debugging zulassen?"* ➔ Setze den Haken bei *"Von diesem Computer immer zulassen"* und tippe auf **Zulassen**.

4. **App auf das Handy installieren**:
   - In Android Studio oben in der Werkzeugleiste dein Smartphone als Zielgerät auswählen.
   - Auf den grünen **Play-Button (Run 'composeApp')** klicken.
   - Die App wird automatisch gebaut, installiert und auf deinem Handy gestartet! 🎉

---

## 📦 Option 2: APK-Datei manuell bauen & auf das Handy übertragen

Wenn du die App ohne Android Studio direkt als `.apk` Datei installieren möchtest:

1. **APK auf dem PC erstellen**:
   - Öffne ein Terminal im Ordner `c:\repos\Veloce\VeloceApp`.
   - Führe folgenden Befehl aus:
     ```bash
     ./gradlew :composeApp:assembleDebug
     ```
   - Die fertige APK liegt anschließend unter:
     `VeloceApp/composeApp/build/outputs/apk/debug/composeApp-debug.apk`

2. **APK aufs Handy übertragen**:
   - Sende dir die `.apk`-Datei per USB, E-Mail, Google Drive oder WhatsApp/Telegram an dein Handy.

3. **Installieren**:
   - Öffne die Datei auf deinem Handy.
   - Falls dein Handy fragt *"Apps aus unbekannten Quellen installieren zulassen?"*, bestätige dies mit **Zulassen**.
   - Tippe auf **Installieren** und öffne die Veloce App!

---

## 🔵 Bluetooth-Verbindung mit dem Arduino

1. Schalte deinen Arduino ein (HC-05 LED blinkt schnell).
2. Öffne auf deinem Handy **Einstellungen** ➔ **Bluetooth** ➔ **Neues Gerät koppeln**.
3. Wähle **HC-05** aus (PIN ist meist `1234` oder `0000`).
4. Starte die **Veloce App** auf deinem Handy, gehe in **Settings**, trage deine Backend-URL ein und starte deine Fahrt! 🏍️💨
