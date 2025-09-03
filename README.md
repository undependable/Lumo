# LUMO

Lumo er en Android-applikasjon som hjelper deg med å beregne strømproduksjon og årlige besparelser fra solcellepaneler på din eiendom. Appen lar deg registrere takflater med ulike vinkler og retninger, og gir deg detaljerte estimater basert på værmønstre og solinnstråling.

---

## Hvordan kjøre appen

1. Åpne terminalen og naviger til området du ønsker å laste ned prosjektet 
2. Kopier URL-en til prosjektet, ved å trykke på "code" knappen på github-siden til prosjektet(https://github.uio.no/IN2000-V25/team-33) og trykk på kopier-knappen
3. Skriv kommandoen `git clone <URL>` i terminalen og trykk enter
4. Trykk enter og vent til nedlastningen er fullført
5. Åpne Android Studio og trykk på "open"
6. Finn frem til der du lagra prosjektet og trykk på "open"
7. Trykk på "run" knappen for å kjøre appen

---

## Avhengigheter og tillatelser

- **Internett** (`INTERNET`): For å hente data fra solcelleberegnings-APIer, værdata og strømpriser
- **Nettverkstilgang** (`ACCESS_NETWORK_STATE`): For å sjekke nettverksstatus
- **Lokasjon** (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`): For å finne brukeren sin posisjon
- **MapBox**

## Biblioteker

### **Ktor (v3.1.1)**
HTTP-klient bibliotek for API-kall. Ktor brukes til å kommunisere med alle eksterne APIer som PVGIS, Frost, og strømpris-APIer. Biblioteket gir robust håndtering av HTTP-forespørsler og JSON-serialisering/deserialisering.

### **Jetpack Compose**
Moderne UI-rammeverk for Android. Brukes for hele brukergrensesnittet med deklarativ og reaktiv UI-programmering.

### **Dagger Hilt (v2.56.1)**
Dependency injection-rammeverk som forenkler og standardiserer dependency management. Spesielt nyttig for å injisere repositories og ViewModels på tvers av app-komponenter.

### **Room Database**
Lokalt databasesystem for å lagre brukerdata som favorittadresser, takflatedata og strømforbruk. Gir persistent lagring selv når appen er stengt.

### **Kotlin Coroutines**
Asynkron programmering som tillater ikke-blokkerende API-kall og database-operasjoner. Sikrer at UI forblir responsiv under datainnhenting.

### **MapBox (v11.10.2)**
Karttjeneste for geografisk visning og plassering av adresser. Brukes for for å håndtere kart, og visualisering av adresser.

### **Compose Charts (Ehsan Narmani v0.1.2)**
Spesialbygd charting-bibliotek for Compose som brukes til å vise månedlige produksjons- og besparelsesgrafer.

### **Glide (v4.16.0)**
Bildehåndtering og animasjonsbibliotek. Brukes for animerte splash-screen elementer og bildeoptimalisering.

### **Navigation Compose**
Navigering mellom skjermer i Compose-miljøet med type-safe argumentpassing og backstack-håndtering.
