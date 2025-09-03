# Arkitektur

## Teknologier:
- Kotlin: Programmeringsspråk.
- Jetpack Compose: Brukt sammen med Kotlin for UI.
- Coroutines: For håndtering av asynkrone oppgaver.
- Hilt: Brukes til dependency injection.
- Room: Brukes til lokal databasehåntering.
- Ktor: Brukes til API-kall.
- MapBox SDK: For interaktivt kart og geolokasjon.

## Arkitekturmønster - MVVM

For å skille logikken for brukergrensesnittet og forretningslogikken, har MVVM (Model - View - ViewModel) blitt brukt som arkitekturmønster.

- Model: Laget ansvarlig for abstraksjon av datakildene. Jobber sammen med ViewModel for å hente og lagre data.

- View: I tillegg til å observere ViewModel, informerer View-laget ViewModel om brukerens handlinger.

- ViewModel: Fungerer som et mellomledd mellom View og Modell. Eksponerer datastrømmer som er relevante for visning.

(Geeks for Geeks, 2025)

I Model-laget har data og logikk blitt håndtert av Room for lokal datalagring, Ktor for API-kall, og repositories som abstraherer disse datakildene. Repositories fungerer som et abstraksjonslag mellom ViewModels og datakilder, og håndterer forretningslogikk som datakonvertering.

View-laget representerer UI-laget som sender brukerhandlinger til ViewModel og viser data til selve brukeren.

ViewModel eksponerer state via StateFlow, håndterer events fra UI, og koordinerer data fra repositories.

## UDF (Unidirectional Data Flow)

Vi har implementert Unidirectional Data Flow (UDF) prinsippet gjennom hele applikasjonen for å sikre konsistent datahåndtering og forutsigbar state-oppdatering.
**Implementering av UDF:**
I vår arkitektur flyter state kun i én retning - fra høyere lag (ViewModel/Repository) ned til UI-laget. Events (brukerhandlinger) flyter i motsatt retning - fra UI opp til ViewModel.

Eksempel på dataflyt:
1. Bruker skriver i søkefelt (Event)
2. UI sender event til ViewModel
3. ViewModel oppdaterer state
4. State flyter ned til UI via StateFlow
5. UI recomposer med ny data

## Objektorienterte prinsipper

### Lav Kobling

Lav kobling handler om at samarbeid mellom komponenter begrenses.

- Ved bruk av repositories har vi isolert datakilder fra resten av applikasjonen.
- Dependency injection har blitt brukt til å redusere direkte avhengigheter.
- StateFlow mønsteret som blir brukt i ViewModels lar UI-komponenter kun observere immutable tilstandsdata gjennom asStateFlow(), uten å ha direkte tilgang til databehandlingen.

### Høy Kohesjon

Høy kohesjon handler om at komponenter skal ha klart definerte ansvarsområder, der antall oppgaver ikke er for mange.

- MVVM har definert klare ansvarsområder.
- I stor grad har komponenter hatt tydelig definerte ansvarsområder:
    - MapPointViewModel: Håndterer MapPoint-logikk.
    - SearchScreenViewModel: Håndterer søkefunksjonalitet.
    - Repositories har hatt ansvar for én og én datakilde.
    - Data Access Object (DAO) har ansvar for hver sin entity, som håndterer lagring i lokal database.

(Android Developers, 2025)


## API
Vi valgte å sette minimum API-nivå til 26 i kodebasen vår. Hovedårsaken til for dette nivået ligger i hvordan vi henter ut strømprisen fra hvakosterstrommen API. For å kunne hente dagens priser og prisen for nåværende time benyttet vi metoder som LocalDate.now(), .hour() og DateTimeFormatter.ofPattern(). Disse metodene er fra Java 8 Time API som krever minimum Android 8.0 for å fungere korrekt, som tilsvarer API-nivå 26 i Android systemet. Vi kompilerer og har targetSdk mot API-nivå 35 for å utnytte de nyeste android funksjonalitetene, som forsikrer at appen vår fungerer optimalt på nyere enheter. Når vi har satt minSdk til 26 vil vi også opprettholde kompatibilitet med eldre android enheter.

## Videreutvikling
- La bruker kunne velge flere takflater (samtidig) for beregning.
- La bruker tegne takflate på kartet.
- Implementere nedbetalingstid.
- Første gang en bruker lagrer en adresse vises ikke loading animasjonen, men istedet er skjermen fryst på WelcomeScreen før den forventede dataen vises. Dette skjer ikke når en adresse allerede er lagret. Dette har sannsynligvis noe å gjøre med loading-tilstander (kan kategoriseres som en bug). 
- I nåværende implementasjon håndterer MapPointViewModel funksjonalitet og tilstander som kanskje kunne blitt flyttet til en annen/ny ViewModel. Koden kan derfor refaktoreres for å bli mer modulær. 


**Kildeliste:**
Geeks for Geeks. *MVVM (Model View ViewModel) Architecture Pattern in Android.* (2025)
https://www.geeksforgeeks.org/mvvm-model-view-viewmodel-architecture-pattern-in-android/

Android Developers. *Common modularization patterns.* (2025)
https://developer.android.com/topic/modularization/patterns


