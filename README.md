# Traffic Light Simulation

Symulacja inteligentnych świateł drogowych na skrzyżowaniu czterech dróg dojazdowych z możliwością konfiguracji liczby pasów. System dynamicznie dostosowuje cykle świateł w oparciu o aktualny stan ruchu i najoptymalniejsze wybory.

## Sposób uruchomienia

### Wymagania

- Zainstalowany i uruchomiony Docker

### Uruchomienie symulacji

**Linux / Mac:**
```bash
./run.sh input.json output.json
```

**Windows (PowerShell):**
```powershell
.\run.ps1 input.json output.json
```

Pierwszy argument to plik wejściowy z komendami (JSON), drugi to ścieżka do pliku wyjściowego, w którym zostaną zapisane wyniki symulacji.

### Uruchomienie testów

**Linux / Mac:**
```bash
./run-tests.sh
```

**Windows (PowerShell):**
```powershell
.\run-tests.ps1
```

## Format wejścia/wyjścia

### Wejście (`input.json`)

```json
{
  "roads": {
    "north": { "leftTurnLane": false, "rightTurnLane": false },
    "south": { "leftTurnLane": true, "rightTurnLane": true },
    "east":  { "leftTurnLane": true, "rightTurnLane": true },
    "west":  { "leftTurnLane": true, "rightTurnLane": false }
  },
  "maxStayTime": 5,
  "fastSimulation": false,
  "commands": [
    { "type": "addVehicle", "vehicleId": "V1", "startRoad": "south", "endRoad": "north" },
    { "type": "step" }
  ]
}
```

- **`roads`** (opcjonalne) - konfiguracja pasów dla każdego kierunku. Każda droga może mieć dodatkowy pas do skrętu w lewo (`leftTurnLane`) i/lub w prawo (`rightTurnLane`). Domyślnie każda droga ma jeden pas obsługujący wszystkie manewry.
- **`maxStayTime`** (opcjonalne, domyślnie 10) - maksymalna liczba kroków, przez które pojazd może czekać, zanim jego pas otrzyma priorytet.
- **`fastSimulation`** (opcjonalne, domyślnie `true`) - przy `false` symulacja zatrzymuje się na 2 sekundy po każdej zmianie stanu, umożliwiając obserwację wizualizacji ASCII w konsoli.
- **`commands`** - lista komend `addVehicle` i `step`.

### Wyjście (`output.json`)

```json
{
  "stepStatuses": [
    { "leftVehicles": ["V1", "V2"] },
    { "leftVehicles": [] }
  ]
}
```

Dla każdej komendy `step` generowany jest wpis z listą pojazdów, które opuściły skrzyżowanie w danym kroku.

## Założenia projektowe

1. **Skrzyżowanie** - cztery kierunki wjazdu (N, S, E, W), z opcjonalnymi dedykowanymi pasami do skrętu w lewo i prawo dla każdego kierunku.
2. **Pasy ruchu** - domyślnie każda droga ma jeden pas, który obsługuje jazdę na wprost, skręt w lewo i w prawo. Włączenie `leftTurnLane` lub `rightTurnLane` wydziela osobny pas z dedykowaną sygnalizacją dla tego manewru, odciążając pas główny.
3. **Cykl świateł** - przy zmianie stanu światło przechodzi przez fazę żółtą (RED → YELLOW → GREEN lub GREEN → YELLOW → RED), co odpowiada rzeczywistemu zachowaniu sygnalizacji.
4. **Bezpieczeństwo** - system gwarantuje, że zielone światło otrzymują jednocześnie wyłącznie pasy o niekolidujących trajektoriach. Jest to zapewnione przez graf kompatybilności oparty na geometrycznej analizie przecięć torów ruchu.
5. **Jeden pojazd na krok** - w każdym kroku symulacji z każdego pasa z zielonym światłem odjeżdża dokładnie jeden pojazd (pierwszy w kolejce, FIFO).
6. **Zapobieganie zagłodzeniu** - parametr `maxStayTime` zapewnia, że żaden pas nie czeka nieskończenie na zielone światło.
7. **Walidacja wejścia** - parser dokładnie waliduje strukturę JSON i komunikuje błędy z kontekstem (np. indeks komendy, nazwa pola).

## Przykładowy układ skrzyżowania jaki możemy zamodelować dzięki opcji konfiguralności pasów
![Intersection](https://github.com/user-attachments/assets/e92d8d99-86e0-422d-b07f-3a3bfab7ee42)

## Opis działania algorytmu

Algorytm wyboru optymalnej konfiguracji świateł opiera się na **teorii grafów** - konkretnie na znajdowaniu **maksymalnych klik** w grafie kompatybilności pasów.

### Krok 1: Graf kompatybilności ruchów (Movement Compatibility Graph)

Przy inicjalizacji skrzyżowania budowany jest graf, w którym wierzchołkami są wszystkie możliwe **ruchy** (pary kierunek wjazdu → kierunek wyjazdu, np. NORTH→SOUTH, EAST→NORTH). Dwa ruchy są połączone krawędzią, jeśli **nie kolidują ze sobą** i mogą bezpiecznie odbywać się jednocześnie.

Detekcja kolizji (`CompatibilityGraph.doConflict`) opiera się na logicznej analizie typów manewrów (`STRAIGHT`, `LEFT`, `RIGHT`) zgodnie z zasadami ruchu prawostronnego:
- **Ten sam wjazd** → brak konfliktu.
- **Ten sam wyjazd** → konflikt.
- **Prosto i skręt w lewo** → konflikt (przecięcie torów jazdy na środku skrzyżowania).
- **Dwa razy prosto** → brak konfliktu dla jazd z naprzeciwka, w przeciwnym razie konflikt (kierunki prostopadłe).
- **Dwa skręty w lewo z naprzeciwka** → brak konfliktu (bezkolizyjne mijanie się wewnątrz skrzyżowania), w przeciwnym razie konflikt.
- **Skręt w prawo** - brak konfliktu z pozostałymi ruchami.

### Tak prezentują się wierzchołki i krawędzie wychodzące od południowej drogi skrzyżowania
![graph](https://github.com/user-attachments/assets/2c5aa726-23bb-44ee-9cc2-7ce5c5052a96)


### Krok 2: Graf kompatybilności pasów (Lane Compatibility Graph)

Z grafu ruchów wyprowadzany jest graf na poziomie **pasów ruchu**. Dwa pasy są kompatybilne wtedy i tylko wtedy, gdy **wszystkie** możliwe ruchy jednego pasa są kompatybilne ze **wszystkimi** możliwymi ruchami drugiego. To podejście gwarantuje bezpieczeństwo - jeśli choćby jedna kombinacja koliduje, pasy nie mogą mieć jednocześnie zielonego.

### Krok 3: Wybór optymalnego zestawu pasów (algorytm Brona-Kerboscha)

Przy każdej komendzie `step` system szuka optymalnego zestawu pasów, które mogą jednocześnie dostać zielone światło. Problem sprowadza się do znalezienia **najlepszej kliki** w grafie kompatybilności pasów:
1. Algorytm **Brona-Kerboscha z pivotowaniem** wylicza wszystkie **maksymalne kliki** (zestawy wzajemnie kompatybilnych pasów, których nie da się rozszerzyć o kolejny pas).
2. Spośród znalezionych klik wybierana jest najlepsza według kaskadowych kryteriów:
   - **Rozmiar kliki** - preferowane są większe kliki (więcej pasów z zielonym = większa przepustowość).
   - **Łączna liczba oczekujących pojazdów** - przy równym rozmiarze, wygrywa klika z większą liczbą pojazdów.
   - **Łączny czas oczekiwania** - ostateczny warunek, faworyzujący dłużej czekające pojazdy.

### Krok 4: Zapobieganie zagłodzeniu

Jeśli pierwszy pojazd na jakimkolwiek pasie czeka dłużej niż `maxStayTime` kroków, ten pas otrzymuje **priorytet**:
1. Spośród pasów z przekroczonym czasem wybierany jest ten z najdłuższym oczekiwaniem (dalej: liczbą pojazdów, łącznym czasem oczekiwania).
2. Algorytm Brona-Kerboscha szuka klik **zawierających** ten priorytetowy pas.
3. Spośród takich klik wybierana jest najlepsza wg tych samych kryteriów co wyżej.

Dzięki temu system balansuje między maksymalizacją przepustowości a sprawiedliwością - żaden kierunek nie jest zagładzany nawet przy dużym natężeniu z innego kierunku.

### Podsumowanie przepływu jednego kroku

```text
step
 ├── Sprawdź, czy są pasy z przekroczonym maxStayTime
 │   ├── TAK → wymuś priorytet dla najdłużej czekającego pasa
 │   │         → szukaj klik zawierających ten pas
 │   └── NIE → szukaj wszystkich maksymalnych klik
 ├── Wybierz najlepszą klikę (rozmiar → pojazdy → czas)
 ├── Faza żółta (dla pasów zmieniających stan)
 ├── Ustaw zielone/czerwone
 ├── Przepuść po jednym pojeździe z każdego zielonego pasa
 └── Inkrementuj czas oczekiwania na pasach z czerwonym
```

## Dodatkowe funkcjonalności

- **Konfigurowalne pasy ruchu** - możliwość definiowania dedykowanych pasów do skrętu w lewo i prawo na każdej drodze dojazdowej, co pozwala modelować bardziej realistyczne skrzyżowania.
- **Wizualizacja ASCII** - każdy stan skrzyżowania jest wyświetlany w konsoli jako kolorowy diagram ASCII z informacjami o stanie świateł, liczbie pojazdów i kierunkach ruchu.
- **Wzorzec Observer** - architektura oparta na obserwatorach (Observer Pattern) umożliwia łatwe dodawanie nowych sposobów śledzenia stanu (wizualizacja, logowanie, monitoring).
- **Docker** - dostępny `Dockerfile` i `docker-compose.yml` do uruchomienia symulacji w kontenerze.
- **Pokrycie testami** - testy jednostkowe dla modelu, silnika, parsera I/O i komend, a także testy integracyjne.

## Struktura projektu

```text
src/main/java/app/
├── Main.java                        # Punkt wejścia
├── Simulation.java                  # Orkiestracja symulacji
├── model/
│   ├── Direction.java               # Enum kierunków (N, S, E, W)
│   ├── Movement.java                # Para (from, to)
│   ├── Vehicle.java                 # Pojazd z czasem oczekiwania
│   ├── TrafficLane.java             # Pas ruchu z kolejką pojazdów
│   ├── TrafficLight.java            # Sygnalizator
│   ├── TrafficLightColour.java      # Enum stanów światła
│   ├── Intersection.java            # Skrzyżowanie - centralny model
│   └── Specification.java           # Specyfikacja symulacji
├── engine/
│   ├── Step.java                    # Logika jednego kroku + wybór świateł
│   ├── CompatibilityGraph.java      # Graf kompatybilności ruchów
│   ├── LaneCompatibilityGraph.java  # Graf kompatybilności pasów + Bron-Kerbosch
│   ├── command/
│   │   ├── Command.java             # Interfejs komendy
│   │   ├── AddVehicleCommand.java   # Dodanie pojazdu
│   │   └── StepCommand.java         # Wykonanie kroku
│   └── observer/
│       ├── IntersectionObserver.java # Interfejs obserwatora
│       ├── AsciiIntersectionObserver.java
│       ├── AsciiIntersectionRenderer.java
│       ├── VehicleStatusObserver.java
│       └── ...                      # Snapshoty i listenery
└── io/
    ├── InputParser.java             # Parser JSON wejściowego
    ├── OutputFormatter.java         # Formatter JSON wyjściowego
    ├── SimulationReport.java        # Raport z wynikami kroków
    ├── RoadConfig.java              # Konfiguracja pasów drogi
    └── InvalidInputException.java   # Wyjątek walidacji
```
