# Modellering og systemdesign

## ARKITEKTURSKISSE
![arkitekturskisse](https://github.uio.no/IN2000-V25/team-33/assets/9353/5fd44d4a-9cf5-4749-a4c4-18dea45d39a1)

## USE CASE
[UseCase.pdf](https://github.uio.no/IN2000-V25/team-33/files/583/UseCase.pdf)

**Use Case: Finn adresse og legg til takflate** \
Primæraktør: Bruker \
Sekundæraktører: Geonorge API, PVGIS API \
Prebetingelse: Ingen \
Postbetingelse: Bruker har lagret bolig med takflate

**Hovedflyt:** 
1. Brukeren åpner applikasjonen
2. Brukeren søker etter spesifikk adresse
3. Applikasjonen henter koordinater og stedsdata fra Geonorge API
4. Brukeren legger til takflate, og oppgir areal, vinkel og retning

**Alternativ flyt:** \
2.1: Brukeren trykker på ønsket bolig direkte på kartet \
2.2: Ingen gyldig addresse ble funnet \
2.3: Brukeren oppgir ny addresse eller avslutter.
git status
&nbsp;

**Use Case: Se statestikk** \
Primæraktør: Bruker \
Sekundæraktører: FrostAPI, PVGIS API, hvakosterstrommen API \
Prebetingelse: Brukeren har lagt til bolig og tilhørende takflate \
Postbetingelse: Brukeren har fått estimert strømproduksjon for takflate(r) basert på soldata og værdata 

**Hovedflyt:**
1. Bruker er inne på applikasjonen
2. Bruker trykker på HJEM-knappen
3. Applikasjonen henter solinstråling og produksjonsdata fra PVGIS api
4. Applikasjonen henter værhistorikk fra Frost API
5. Applikasjonen henter strømprisinformasjon fra hvakosterstrommen API
6. Systemet viser relevant statistikk for brukerens takflater


&nbsp;

**Use Case: Legg til ny takflate** \
Primæraktør: Bruker \
Sekundæraktører: PVGIS API \
Prebetingelse: Brukeren har lagt til bolig \
Postbetingelse: En ny takflate er lagt til 

**Hovedflyt:**
1. Bruker er inne på applikasjonen
2. Bruker trykker på HJEM-knappen
3. Bruker trykker på "Takflater"
4. Bruker trykker på "Legg til ny takflate"
5. Brukeren legger til takflate, og oppgir areal, vinkel og retning

&nbsp;

**Use Case: Velg mellom takflater** \
Primæraktør: Bruker \
Sekundæraktører: \
Prebetingelse: Brukeren har lagt til bolig og minst 2 takflater \
Postbetingelse: Statestikken er oppdatert med nyeste data 

**Hovedflyt:**
1. Bruker er inne på applikasjonen
2. Bruker trykker på HJEM-knappen
3. Bruker trykker på "Takflater"
4. Bruker velger hvilken takflate hen vil se statestikk for ved å trykke på checkmark ved relevant takflate


## KLASSEDIAGRAM
```mermaid
classDiagram
direction TB

    %% UI Layer - Main Activity
    class MainActivity {
        searchScreenViewModel: SearchScreenViewModel
        mapPointViewModel: MapPointViewModel
        profileViewModel: ProfileViewModel
        fusedLocationClient: FusedLocationProviderClient
        onCreate(Bundle?)
        requestLocationPermissions()
    }

    %% UI Layer - Screens
    class HomeScreen {
        HomeScreen(NavController, MapPointViewModel, NetworkViewModel, ProfileViewModel)
        MainHomeContent()
        WelcomeHomeScreen()
        LoadingScreen()
    }

    class SearchScreen {
        MapScreen(NavController, SearchScreenViewModel, NetworkViewModel, MapPointViewModel, FusedLocationProviderClient)
        InfoModal()
        AddresseModal()
    }

    class ProfileScreen {
        ProfileScreen(NavController, ProfileViewModel)
        ProfileCard()
    }

    class StatisticsScreen {
        StatisticsScreen(ProfileViewModel, NavController, NetworkViewModel, MapPointViewModel)
        AnnualSavingsSection()
        MonthlyProductionSection()
    }

    class SavedScreen {
        SavedScreen(NavController, NetworkViewModel, SearchScreenViewModel, MapPointViewModel)
        SavedPointCard()
    }

    class PowerConsumptionScreen {
        PowerConsumptionScreen(NavController, ProfileViewModel)
        MonthInputRow()
        CustomTextField()
    }

    class UserInfoScreen {
        UserInfoScreen(NavController, ProfileViewModel)
        EditableUserInfoField()
    }

    %% Presentation Layer - ViewModels
    class ProfileViewModel {
        userRepository: UserRepository
        updateUserInfo(UserInfo)
        saveYearlyConsumption()
        saveMonthlyConsumption()
        updateYearlyConsumption(Int)
        getMonthlyConsumptionValues(): List~Double~
    }

    class MapPointViewModel {
        mapPointRepository: MapPointRepository
        frostRepository: FrostRepository
        pvgisRepository: PvgisRepository
        electricityPriceRepository: ElectricityPriceRepository
        selectPoint(MapPoint)
        beregnProduksjonMedVinkel(MapPoint, Int, Double, Int)
        toggleFavorite(MapPoint)
        addMapPoint(Adresser): MapPoint
        loadCurrentTemperature(MapPoint)
        calculateMonthlySavingsFromJson(Context)
    }

    class SearchScreenViewModel {
        addressRepository: AddressRepository
        getAddressuggestions(String): List~Adresser~
        getAddressFromCoordinates(Double, Double): Adresser?
        handleLocationButtonClick(Context, FusedLocationProviderClient, Function)
        updateSearchBarText(String)
        updateSelectedMapPoint(MapPoint?)
    }

    class NetworkViewModel {
        checkConnectivity(Context)
    }

    %% Domain Layer - Repositories
    class UserRepository {
        userInfoDao: UserInfoDao
        powerConsumptionDao: PowerConsumptionDao
        getUserInfo(): Flow~UserInfo?~
        updateUserInfo(UserInfo)
        getPowerConsumption(): Flow~PowerConsumptionEntity?~
        updatePowerConsumption(PowerConsumptionEntity)
    }

    class MapPointRepository {
        mapPointDao: MapPointDao
        saveMapPointWithTakflate(MapPoint): Int
        deleteMapPoint(MapPoint)
        getMapPointsWithTakflateData(): Flow~List~MapPoint~~
    }

    class AddressRepository {
        dataSource: AddressDataSource
        getAddressCoordinates(String): Representasjonspunkt?
        getAddressInformation(String): Adresser?
        getAllAddresses(String): List~Adresser~?
    }

    class PvgisRepository {
        dataSource: PvgisDataSource
        frostRepository: FrostRepository
        calculateAdjustedAnnualProduction(Double, Double, Double, Double, Int, Int): Double?
        calculateAdjustedMonthlyProduction(Double, Double, Double, Double, Int, Int): List~Pair~Int,Double~~?
        adjustIrradianceForCloudAndSnow(Double, Double, Double): Double
    }

    class FrostRepository {
        dataSource: FrostDataSource
        getStationId(Representasjonspunkt): String
        getCurrentTemperature(String): Double
        getWeatherData(Double, Double): Map~WeatherType,List~Double~~
    }

    class ElectricityPriceRepository {
        electricityDataSource: ElectricityPriceDataSource
        fetchPricesToday(String): List~priceItem~
        fetchHourPrice(String): String
        fetchAverageSSBPriceFor2024(): String
    }

    %% Data Layer - DataSources
    class PvgisDataSource {
        fetchHourlyData(Double, Double, Int, Int): PvgisResponse?
        fetchProductionData(Double, Double, Double, Int, Int, Int): PvgisResponse?
        clearCache()
    }

    class FrostDataSource {
        getWeatherStation(Representasjonspunkt): WeatherStation
        getCurrentWeather(String): ObservationResponse
        getObservations(String, String, String, String, String): ObservationResponse
    }

    class AddressDataSource {
        getAddressResponse(String): AddressResponse
        getAddressFromPoint(Double, Double): AddressResponse
    }

    class ElectricityPriceDataSource {
        fetchPricesToday(String): List~priceItem~
        fetchQuarterlyPricesSSB(): List~Pair~String,Double~~
        getMonthlyPriceMapForRegion(Context, String): Map~String,Double~
    }

    %% Data Layer - Database
    class UserDatabase {
        getUserInfoDao(): UserInfoDao
        getMapPointDao(): MapPointDao
        getPowerConsumptionDao(): PowerConsumptionDao
    }

    class UserInfoDao {
        getUserInfo(): Flow~UserInfo?~
        upsertUserInfo(UserInfo)
    }

    class MapPointDao {
        getMapPointsWithTakflateData(): Flow~List~MapPointWithTakflateData~~
        upsertMapPoint(MapPointEntity): Long
        deleteMapPoint(MapPointEntity)
    }

    class PowerConsumptionDao {
        getPowerConsumption(): Flow~PowerConsumptionEntity?~
        upsertPowerConsumption(PowerConsumptionEntity)
    }



    %% Relationships
    MainActivity "1" --> "1" SearchScreenViewModel
    MainActivity "1" --> "1" MapPointViewModel  
    MainActivity "1" --> "1" ProfileViewModel

    HomeScreen "1" --> "1" MapPointViewModel
    HomeScreen "1" --> "1" NetworkViewModel
    HomeScreen "1" --> "1" ProfileViewModel

    SearchScreen "1" --> "1" SearchScreenViewModel
    SearchScreen "1" --> "1" NetworkViewModel
    SearchScreen "1" --> "1" MapPointViewModel

    ProfileScreen "1" --> "1" ProfileViewModel
    StatisticsScreen "1" --> "1" ProfileViewModel
    StatisticsScreen "1" --> "1" MapPointViewModel
    SavedScreen "1" --> "1" MapPointViewModel
    PowerConsumptionScreen "1" --> "1" ProfileViewModel
    UserInfoScreen "1" --> "1" ProfileViewModel

    ProfileViewModel "1" --> "1" UserRepository
    MapPointViewModel "1" --> "1" MapPointRepository
    MapPointViewModel "1" --> "1" FrostRepository
    MapPointViewModel "1" --> "1" PvgisRepository
    MapPointViewModel "1" --> "1" ElectricityPriceRepository
    SearchScreenViewModel "1" --> "1" AddressRepository

    UserRepository "1" --> "1" UserInfoDao
    UserRepository "1" --> "1" PowerConsumptionDao
    MapPointRepository "1" --> "1" MapPointDao
    AddressRepository "1" --> "1" AddressDataSource
    PvgisRepository "1" --> "1" PvgisDataSource
    PvgisRepository "1" --> "1" FrostRepository
    FrostRepository "1" --> "1" FrostDataSource
    ElectricityPriceRepository "1" --> "1" ElectricityPriceDataSource

    UserDatabase "1" --> "1" UserInfoDao : provides
    UserDatabase "1" --> "1" MapPointDao : provides
    UserDatabase "1" --> "1" PowerConsumptionDao : provides
```


## SEKVENSDIAGRAM 
**bruker søker opp en adresse, legger til takflate og blir sendt til homescreen med representativ statestikk**  


```mermaid
sequenceDiagram
    actor User
    participant App as Lumo App
    participant SearchVM as SearchScreenViewModel
    participant MapPointVM as MapPointViewModel
    participant AddressAPI as Address Repository
    participant FrostAPI as Frost Repository
    participant PVGISAPI as PVGIS Repository
    participant RoomDB as ROOM Database

    User->>App: Enter address in search bar
    App->>SearchVM: onAddressEntered(addressText)
    SearchVM->>AddressAPI: getAddressuggestions(addressText)
    
    alt Multiple address suggestions
        AddressAPI-->>SearchVM: Return list of addresses
        SearchVM-->>App: Update UI with suggestions
        App-->>User: Display address suggestions
        User->>App: Select address from suggestions
        App->>SearchVM: onAddressChosen(selectedAddress)
        SearchVM->>SearchVM: clearAddressSuggestions()
        SearchVM->>MapPointVM: addMapPoint(selectedAddress)
    else Single address result
        AddressAPI-->>SearchVM: Return single address
        SearchVM->>MapPointVM: addMapPoint(singleAddress)
    else No valid address found
        AddressAPI-->>SearchVM: Return empty list
        SearchVM-->>App: showError(INVALID_ADDRESS)
        App-->>User: Display error message
    end
    
    MapPointVM->>MapPointVM: Create MapPoint
    MapPointVM-->>SearchVM: Return created MapPoint
    SearchVM->>SearchVM: updateSelectedMapPoint(mapPoint)
    SearchVM-->>App: Show map with selected point
    
    MapPointVM->>FrostAPI: loadCurrentTemperature(mapPoint)
    FrostAPI-->>MapPointVM: Return temperature data
    MapPointVM-->>App: Update UI with temperature
    
    App-->>User: Display address bottom sheet
    
    User->>App: Click "Legg til takflate"
    App-->>User: Show takflate sheet
    
    User->>App: Enter takflate details (angle, area, direction)
    User->>App: Click "Lagre takflate" button
    App->>MapPointVM: beregnProduksjonMedVinkel(mapPoint, angle, area, direction)
    MapPointVM->>PVGISAPI: beregnJustertProduksjon(lat, lon, area, efficiency, angle, aspect)
    PVGISAPI-->>MapPointVM: Return annual production estimate
    MapPointVM->>MapPointVM: Add takflate to MapPoint
    MapPointVM->>RoomDB: mapPointRepository.saveMapPointWithTakflate(updatedMapPoint)
    RoomDB-->>MapPointVM: Confirm storage
    MapPointVM-->>App: Update UI with production data
    App->>App: Navigate to HomeScreen
    App->>MapPointVM: loadHomeScreenData()
    MapPointVM->>MapPointVM: Calculate updated statistics
    MapPointVM-->>App: Return updated statistics (production, savings)
    App-->>User: Display HomeScreen with updated statistics

``` 

&nbsp;
&nbsp;

**Bruker ønsker å se statestikk**

```mermaid
sequenceDiagram
    actor User
    participant HomeScreen
    participant StatsScreen as StatisticsScreen
    participant MapPointVM as MapPointViewModel
    participant ProfileVM as ProfileViewModel
    participant PVGISAPI as PVGIS Repository
    participant ElectricityAPI as Electricity Price Repository
    participant RoomDB as ROOM Database

    User->>HomeScreen: Open app / Navigate to HomeScreen
    
    HomeScreen->>MapPointVM: loadHomeScreenData()
    
    alt Has saved MapPoints in database
        MapPointVM->>RoomDB: getMapPointsWithTakflateData()
        RoomDB-->>MapPointVM: Return saved MapPoints
        
        MapPointVM->>ElectricityAPI: fetchHourPrice(mapPoint.region)
        ElectricityAPI-->>MapPointVM: Return current electricity price
        
        MapPointVM->>PVGISAPI: loadMonthlyProduction (if not already loaded)
        PVGISAPI-->>MapPointVM: Return monthly production data
        
        MapPointVM-->>HomeScreen: Update UI with MapPoint data
        HomeScreen-->>User: Display HomeScreen with statistics chart
    else No saved MapPoints
        RoomDB-->>MapPointVM: Return empty list
        MapPointVM-->>HomeScreen: Update UI with empty state
        HomeScreen-->>User: Display welcome screen
    end
    
    User->>HomeScreen: Click "Statistikk" card
    HomeScreen->>StatsScreen: Navigate to StatisticsScreen
    
    StatsScreen->>MapPointVM: ensureMonthlyDataLoaded()
    
    alt Monthly data not loaded
        MapPointVM->>PVGISAPI: beregnJustertMaanedsproduksjon(lat, lon, areal, ...)
        PVGISAPI-->>MapPointVM: Return monthly production data
    end
    
    StatsScreen->>MapPointVM: calculateMonthlySavingsFromJson(context)
    MapPointVM->>MapPointVM: Process monthly savings calculations
    
    StatsScreen->>ProfileVM: getMonthlyConsumptionValues()
    ProfileVM-->>StatsScreen: Return consumption values
    
    StatsScreen->>MapPointVM: beregnAarligBesparelseFraMaaneder(context)
    MapPointVM->>MapPointVM: Calculate annual savings
    MapPointVM-->>StatsScreen: Return annual savings estimate
    
    StatsScreen-->>User: Display statistics with production and savings data
```


## AKTIVITETSDIAGRAM

__**bruker søker opp en adresse og lagrer takflate**__

```mermaid
flowchart TD;

Start([Start: User enters SearchScreen])

ChooseMethod[Choose Input Method]
InputMethod{Input Method?}

EnterAddress[Enter address in searchbar]  
SubmitSearch[Submit search]

ValidAddress{Valid address?}
ShowError[Show 'Invalid address' error]
CorrectText[User corrects search text]

NumberMatches{Number of matches?}
ShowSuggestions[Show address suggestions]
SelectSuggestion[User selects suggestion]

ClickMap[Get coordinates from click]
FetchAddress[Fetch address for coordinates]

AddTakflate[Add takflate]

CreateMapPoint[Create MapPoint]
NavigateHome[Navigate to HomeScreen]
End([End])

Start-->ChooseMethod
ChooseMethod-->InputMethod
InputMethod--Searchbar-->EnterAddress
EnterAddress-->SubmitSearch  
SubmitSearch-->ValidAddress
ValidAddress--No-->ShowError
ShowError-->CorrectText
CorrectText-->SubmitSearch
ValidAddress--Yes-->NumberMatches
NumberMatches--Single match-->CreateMapPoint
NumberMatches--Multiple matches-->ShowSuggestions
ShowSuggestions-->SelectSuggestion
SelectSuggestion-->CreateMapPoint
InputMethod--Click on map-->ClickMap
ClickMap-->FetchAddress
FetchAddress-->CreateMapPoint
CreateMapPoint-->AddTakflate
AddTakflate-->NavigateHome
NavigateHome-->End
```

&nbsp;
&nbsp;
#
**Overordnet funksjonalitet**
```mermaid
flowchart TD;

Start([Start: User opens app])

ShowHomeWithStatistics[Show home screen with statistics]

UserNavigates{User navigates to?}

ViewStatistics([User views statistics])

ManageTakflater([User manages takflater])

NavigateToProfile([User goes to profile])

NavigateToSearch([User goes to search screen])

ShowStatisticsScreen[Show statistics screen]

ShowTakflateScreen[Show takflate screen]

SelectTakflate([User selects takflate for statistics])

UpdateStatisticsWithTakflate[Update statistics with selected takflate]

ShowProfile[Show profile screen]

ProfileAction{Profile action?}

ToggleDarkMode([User toggles dark mode])

UpdateTheme[Update app theme]

NavigateToPowerConsumption([User clicks power consumption])

ShowPowerConsumption[Show power consumption screen]

UpdateConsumption([User updates consumption])

SaveConsumption[Save consumption data]

UpdateStatisticsWithConsumption[Update statistics with new consumption]

ShowSearchScreen[Show search screen]

SearchAddress([User searches address])

AddTakflate([User adds takflate])

ContinueOrExit{Continue using app?}

End([End: User exits app])

 

Start-->ShowHomeWithStatistics

ShowHomeWithStatistics-->UserNavigates

 

UserNavigates--View Statistics-->ViewStatistics

UserNavigates--Manage Takflater-->ManageTakflater

UserNavigates--Profile-->NavigateToProfile

UserNavigates--Search-->NavigateToSearch

UserNavigates--Exit-->End

 

ViewStatistics-->ShowStatisticsScreen

ShowStatisticsScreen-->ContinueOrExit

 

ManageTakflater-->ShowTakflateScreen

ShowTakflateScreen-->SelectTakflate

SelectTakflate-->UpdateStatisticsWithTakflate

UpdateStatisticsWithTakflate-->ShowHomeWithStatistics

 

NavigateToProfile-->ShowProfile

ShowProfile-->ProfileAction

 

ProfileAction--Toggle Dark Mode-->ToggleDarkMode

ProfileAction--Power Consumption-->NavigateToPowerConsumption

ProfileAction--Back-->ContinueOrExit

 

ToggleDarkMode-->UpdateTheme

UpdateTheme-->ShowProfile

 

NavigateToPowerConsumption-->ShowPowerConsumption

ShowPowerConsumption-->UpdateConsumption

UpdateConsumption-->SaveConsumption

SaveConsumption-->UpdateStatisticsWithConsumption

UpdateStatisticsWithConsumption-->ShowHomeWithStatistics

 

NavigateToSearch-->ShowSearchScreen

ShowSearchScreen-->SearchAddress

SearchAddress-->AddTakflate

AddTakflate-->ShowHomeWithStatistics

 

ContinueOrExit--Yes-->UserNavigates

ContinueOrExit--No-->End

```


