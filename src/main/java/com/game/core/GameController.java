package main.java.com.game.core;

import main.java.com.game.model.Character;
import main.java.com.game.model.Event;
import main.java.com.game.model.EventOption;
import main.java.com.game.model.GameData;
import main.java.com.game.utils.EventLoader;
import main.java.com.game.utils.GameBackup;
import main.java.com.game.utils.StatusLoader;
import main.java.com.game.utils.TextUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameController {
    private final GameData gameData;
    private final GameBackup gameBackup;
    private List<Event> events;
    private final Random random = new Random();
    private Event currentDayEvent;
    private Set<String> unlockedEvents = new HashSet<>();
    private Map<String, String> eventSequences = new HashMap<>();
    private Map<String, String> dailyStatusMessages = new HashMap<>();
    private int lastEventChoice = -1;

    // === INICJALIZACJA GRY ===
    public GameController() {
        this.gameData = new GameData();
        this.gameBackup = new GameBackup();
        try {
            this.events = EventLoader.loadAllEvents();
            if (this.events.isEmpty()) {
                System.err.println("Brak eventów! Ładuję domyślne.");
                this.events = createDefaultEvents();
            }
        } catch (IOException e) {
            System.err.println("Błąd ładowania eventów: " + e.getMessage());
            this.events = createDefaultEvents();
        }
        initializeEventChains();
    }
    public void startGame(Scanner scanner) {
        initializeCharacters();

        while(gameData.getCurrentDay() <= 50 && !isGameOver()) {
            System.out.println("\n=== DZIEŃ " + gameData.getCurrentDay() + " ===");
            simulateDay(scanner);
            gameData.setCurrentDay(gameData.getCurrentDay() + 1);
        }

        System.out.println("\nKONIEC GRY!");
    }


    // === ZARZĄDZANIE POSTACIAMI ===
    private void initializeCharacters() {
        gameData.getCharacters().add(new Character("Daniel"));
        gameData.getCharacters().add(new Character("Monika"));
    }
    private void handleNeeds(Scanner scanner) {
        boolean needsExist = gameData.getCharacters().stream()
                .anyMatch(c -> c.isHungry() || c.isThirsty() || c.isSick());

        if (!needsExist) return;

        System.out.println("\n[ZASPOKAJANIE POTRZEB]");
        for(int i = 0; i < gameData.getCharacters().size(); i++) {
            Character character = gameData.getCharacters().get(i);

            // Obsługa głodu
            if(character.isHungry()) {
                System.out.print("Nakarmić " + character.getName() + "? (T/N) ");
                String choice = scanner.nextLine().trim().toUpperCase();
                if(choice.equals("T")) {
                    giveFood(i);
                }
            }

            // Obsługa pragnienia
            if(character.isThirsty()) {
                System.out.print("Napoić " + character.getName() + "? (T/N) ");
                String choice = scanner.nextLine().trim().toUpperCase();
                if(choice.equals("T")) {
                    giveWater(i);
                }
            }

            // Obsługa choroby
            if(character.isSick()) {
                System.out.print("Podaj leki " + character.getName() + "? (T/N) ");
                String choice = scanner.nextLine().trim().toUpperCase();
                if(choice.equals("T")) {
                    giveMedicine(i);
                }
            }
        }
    }
    private void giveFood(int characterIndex) {
        if(gameData.getFoodSupplies() > 0) {
            gameData.getCharacters().get(characterIndex).feed();
            gameData.setFoodSupplies(gameData.getFoodSupplies() - 1);
            System.out.println("Nakarmiono " + gameData.getCharacters().get(characterIndex).getName());
        } else {
            System.out.println("Brak jedzenia!");
        }
    }
    private void giveWater(int characterIndex) {
        if(gameData.getWaterSupplies() > 0) {
            gameData.getCharacters().get(characterIndex).setThirsty(false);
            gameData.setWaterSupplies(gameData.getWaterSupplies() - 1);
            System.out.println("Napojono " + gameData.getCharacters().get(characterIndex).getName());
        } else {
            System.out.println("Brak wody!");
        }
    }
    private void giveMedicine(int characterIndex) {
        if(gameData.getMedicineSupplies() > 0) {
            gameData.getCharacters().get(characterIndex).setSick(false);
            gameData.setMedicineSupplies(gameData.getMedicineSupplies() - 1);
            System.out.println("Wyleczono " + gameData.getCharacters().get(characterIndex).getName());
        } else {
            System.out.println("Brak leków!");
        }
    }
    private void applyDailyEffects() {
        gameData.getCharacters().forEach(Character::applyDailyEffects);
    }

    // === SYMULACJA DNIA ===
    private void simulateDay(Scanner scanner) {
        clearScreen();

        // 1. Ustaw potrzeby na początku dnia
        if (gameData.getCurrentDay() % 2 == 0) {
            System.out.println("--> Sprawdzam głód...");
            gameData.getCharacters().forEach(c -> c.setHungry(true));
        }
        if (gameData.getCurrentDay() % 3 == 0) {
            System.out.println("--> Sprawdzam pragnienie...");
            gameData.getCharacters().forEach(c -> c.setThirsty(true));
        }

        // Generuj wiadomości statusowe
        generateDailyStatusMessages();

        // 2. Losuj wydarzenie dnia PRZED backupem
        currentDayEvent = getRandomEvent();
        if (currentDayEvent == null) {
            currentDayEvent = findEventById("default");
        }

        // Backup stanu PO ustawieniu potrzeb i PO wylosowaniu wydarzenia
        gameBackup.backupState(
                gameData,
                currentDayEvent,
                unlockedEvents,
                eventSequences,
                dailyStatusMessages,
                lastEventChoice
        );

        boolean dayCompleted = false;
        while(!dayCompleted) {
            // 3. Wyświetl status
            displayStatus();

            // 4. Przeprowadź wydarzenie
            if (currentDayEvent != null) {
                System.out.println("\n[WYDARZENIE]");
                System.out.println(currentDayEvent.getDescription());

                List<EventOption> options = currentDayEvent.getOptions();

                // Automatyczny wybór jeśli istnieje zapamiętany
                if (lastEventChoice != -1) {
                    System.out.println("Automatyczny wybór: " + (lastEventChoice + 1));
                    applyEffects(options.get(lastEventChoice).getEffects());

                    if (currentDayEvent.isUnique()) {
                        unlockedEvents.add(currentDayEvent.getId());
                    }
                }
                // Ręczny wybór
                else {
                    for (int i = 0; i < options.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, options.get(i).getText());
                    }

                    int choice = -1;
                    while (choice < 0 || choice >= options.size()) {
                        System.out.print("Twój wybór (1-" + options.size() + "): ");
                        try {
                            choice = scanner.nextInt() - 1;
                            scanner.nextLine();
                            lastEventChoice = choice; // Zapamiętaj wybór
                        } catch (InputMismatchException e) {
                            scanner.nextLine();
                            System.out.println("Nieprawidłowy wybór!");
                        }
                    }

                    applyEffects(options.get(choice).getEffects());

                    if (currentDayEvent.isUnique()) {
                        unlockedEvents.add(currentDayEvent.getId());
                    }
                }
            }

            // 5. Zaspokajanie potrzeb
            handleNeeds(scanner);

            // 6. Potwierdzenie zakończenia dnia
            dayCompleted = confirmDayEnd(scanner);

            // 7. Przywrócenie stanu jeśli nie potwierdzono
            if(!dayCompleted) {
                gameBackup.restoreState(gameData);
                currentDayEvent = gameBackup.getBackupCurrentDayEvent();
                unlockedEvents = new HashSet<>(gameBackup.getBackupUnlockedEvents());
                eventSequences = new HashMap<>(gameBackup.getBackupEventSequences());
                lastEventChoice = gameBackup.getBackupLastEventChoice();
                System.out.println("\n--- Powrót do początku dnia ---");
            } else {
                lastEventChoice = -1;
            }
        }

        // 8. Zastosuj efekty końca dnia
        applyDailyEffects();
    }

    // === OBSŁUGA WYDARZEŃ ===
    public Event getRandomEvent() {
        // 1. Sprawdź czy mamy oczekujący event w sekwencji
        for (String unlocked : new HashSet<>(unlockedEvents)) {
            String nextId = eventSequences.get(unlocked);
            // Zmiana warunku: sprawdzamy czy następnik istnieje i nie jest jeszcze odblokowany
            if (nextId != null && !unlockedEvents.contains(nextId)) {
                Event nextEvent = findEventById(nextId);
                if (nextEvent != null) {
                    unlockedEvents.add(nextId); // Odblokuj następny etap
                    return nextEvent;
                }
            }
        }

        // 2. Standardowe losowanie
        List<Event> availableEvents = events.stream()
                .filter(e -> !e.isHidden() || unlockedEvents.contains(e.getId()))
                .filter(e -> !e.isUnique() || !unlockedEvents.contains(e.getId()))
                .filter(e -> random.nextDouble() <= e.getFrequency())
                .collect(Collectors.toList());

        return availableEvents.isEmpty() ? null : availableEvents.get(random.nextInt(availableEvents.size()));
    }
    private List<Event> createDefaultEvents() {
        return List.of(
                new Event("default", "Nic się nie wydarzyło.", List.of(
                        new EventOption("OK", Map.of())
                ), false, 1.0, false)
        );
    }
    private void applyEffects(Map<String, String> effects) {
        for (Map.Entry<String, String> entry : effects.entrySet()) {
            String effect = entry.getKey();
            String value = entry.getValue();
            final String finalValue;

            try {
                // obsługa losowych wartości
                if (value.startsWith("rand(") && value.endsWith(")")) {
                    String range = value.substring(5, value.length() - 1);
                    String[] parts = range.split(",");
                    if (parts.length == 2) {
                        int min = Integer.parseInt(parts[0].trim());
                        int max = Integer.parseInt(parts[1].trim());
                        value = String.valueOf(random.nextInt(max - min + 1) + min);
                    }
                }
                finalValue = value;

                switch (effect) {
                    // Efekty liczbowe
                    case "+food":
                        gameData.setFoodSupplies(gameData.getFoodSupplies() + Integer.parseInt(value));
                        System.out.println("+ " + value + " jedzenia");
                        break;
                    case "-food":
                        gameData.setFoodSupplies(Math.max(0, gameData.getFoodSupplies() - Integer.parseInt(value)));
                        System.out.println("- " + value + " jedzenia");
                        break;
                    case "+health":
                        gameData.getCharacters().forEach(c -> {
                            int newHealth = Math.min(100, c.getHealth() + Integer.parseInt(finalValue));
                            c.setHealth(newHealth);
                        });
                        System.out.println("+ " + finalValue + " zdrowia");
                        break;
                    case "-health":
                        gameData.getCharacters().forEach(c -> {
                            c.setHealth(Math.max(0, c.getHealth() - Integer.parseInt(finalValue)));
                        });
                        System.out.println("- " + finalValue + " zdrowia");
                        break;
                    case "+water":
                        gameData.setWaterSupplies(gameData.getWaterSupplies() + Integer.parseInt(value));
                        System.out.println("+ " + value + " wody");
                        break;
                    case "-water":
                        gameData.setWaterSupplies(Math.max(0, gameData.getWaterSupplies() - Integer.parseInt(value)));
                        System.out.println("- " + value + " wody");
                        break;
                    case "+medicine":
                        gameData.setMedicineSupplies(gameData.getMedicineSupplies() + Integer.parseInt(value));
                        System.out.println("+ " + value + " leków");
                        break;
                    case "-medicine":
                        gameData.setMedicineSupplies(Math.max(0, gameData.getMedicineSupplies() - Integer.parseInt(value)));
                        System.out.println("- " + value + " leków");
                        break;

                    // Efekty całorodzinne
                    case "feed_all":
                        int familySize = gameData.getCharacters().size();
                        if (gameData.getFoodSupplies() >= familySize) {
                            gameData.setFoodSupplies(gameData.getFoodSupplies() - familySize);
                            gameData.getCharacters().forEach(Character::feed);
                            System.out.printf("- Zużyto dodatkowo %d jedzenia!%n", familySize);
                            System.out.println("Wszyscy zostali nakarmieni!");
                        } else {
                            System.out.println("Nie ma wystarczająco jedzenia!");
                        }
                        break;
                    case "quench_all":
                        int familySizeWater = gameData.getCharacters().size();
                        if (gameData.getWaterSupplies() >= familySizeWater) {
                            gameData.setWaterSupplies(gameData.getWaterSupplies() - familySizeWater);
                            gameData.getCharacters().forEach(c -> c.setThirsty(false));
                            System.out.printf("- Zużyto dodatkowo %d wody!%n", familySizeWater);
                            System.out.println("Wszyscy zostali napojeni!");
                        } else {
                            System.out.println("Nie ma wystarczająco wody!");
                        }
                        break;
                    case "sick_all":
                        gameData.getCharacters().forEach(c -> c.setSick(true));
                        System.out.println("Wszyscy zachorowali!");
                        break;
                    case "cure_all":
                        int familySizeMed = gameData.getCharacters().size();
                        if (gameData.getMedicineSupplies() >= familySizeMed) {
                            gameData.setMedicineSupplies(gameData.getMedicineSupplies() - familySizeMed);
                            gameData.getCharacters().forEach(c -> c.setSick(false));
                            System.out.printf("- Zużyto dodatkowo %d leków!%n", familySizeMed);
                            System.out.println("Wszyscy zostali wyleczeni");
                        } else {
                            System.out.println("Nie ma wystarczająco leków!");
                        }
                        break;

                    // Efekty specjalne (tekstowe)
                    case "unlock":
                        unlockedEvents.add(value);
                        System.out.println("Odblokowano wydarzenie: " + value);
                        break;
                    case "sick":
                        if (!gameData.getCharacters().isEmpty()) {
                            int index = random.nextInt(gameData.getCharacters().size());
                            gameData.getCharacters().get(index).setSick(true);
                            System.out.println(gameData.getCharacters().get(index).getName() + " zachorował!");
                        }
                        break;
                    case "cure":
                        gameData.getCharacters().forEach(c -> c.setSick(false));
                        System.out.println("Wszyscy zostali wyleczeni!");
                        break;
                    case "game_effect":
                        handleGameEffect(value);
                        break;
                    default:
                        System.out.println("Nieznany efekt: " + effect + ":" + value);
                }
            } catch (NumberFormatException e) {
                System.err.println("Błąd parsowania efektu " + effect + ": " + value);
            }
        }
    } // applyEffects
    private void handleGameEffect(String effect) {
        switch (effect) {

            // Secret Room Event
            case "secret_room_etap1no":
                System.out.println("Nie widzieliście sensu żeby otwierać te drzwi, może i dobrze");
                break;
            case "secret_room_etap2no":
                System.out.println("Po co się męczyć by otworzyć jakąś starą skrzynię? Mamy lepsze rzeczy do robienia!");
                break;
            case "secret_room_etap3no":
                System.out.println("Lepiej nie wychodzić ze schronu tylko po to by zobaczyć czy jakaś stara mapa " +
                        "jest nawet jeszcze nadal poprawna, czy tam gdzie ona prowadzi są jeszcze zasoby.");
                break;
            case "secret_room_etap1yes":
                System.out.println("W sumie czemu by nie sprawdzić? A nóż coś wartościowego może tutaj znajdziemy!");
                break;
            case "secret_room_etap2yes":
                System.out.println("Trochę się pomęczyliście ale udało się otworzyć sejf... okazało się, że ta stara " +
                        "skrzynia zawiera mapę do ukrytego magazynu! Przyjżycie się temu jednak kiedy indziej, na " +
                        "dziś tyle.");
                break;
            case "secret_room_etap3yes":
                System.out.println("Mimo trochę niebezpiecznej drogi udało wam się ostatecznie natrafić na " +
                        "zaznaczone krzyżykiem miejsce na mapie i hura! 3 leki i 5 jedzenia! To dopiero skarb!");
                break;

            // Default Events
            case "door_event_gain":
                System.out.println("Uff! W drzwiach stał sprzedawca oferujący wam pożywienie za niewiele złota.");
                break;
            case "door_event_loss":
                System.out.println("Jednak lepiej było nie otwierać... to byli bandyci. Podczas obrony zaginęło parę " +
                        "rzeczy... trzeba być ostrożniejszym następnym razem...");
                break;

            // Lucky Events
            case "loyal_dog":
                System.out.println("Ta biedna psinka jest wniebowzięta i jak wszyscy zauważyli... jest nowym " +
                        "członkiem rodziny!" +
                        ".");
                break;

            // Tricky Events
            case "generator_parts":
                System.out.println("Byłeś niezdarny i się skaleczyłeś... ała...");
                break;

            // Group Events
            case "scientist_group":
                System.out.println("Po pomocy naukowcom, udaje się im osiągnąć to co chcieli osiągnąć.");
                System.out.println("Do tej pory nie wiecie co próbowali zrobić ale darmowe jedzenie nie przeszkadza.");
                break;
            case "church_group":
                System.out.println("Pomogliście grupie ocalałych, są oni bardzo wdzięczni!");
            default:
                System.out.println("Nieznany efekt gry: " + effect);
        }
    }
    private Event findEventById(String eventId) {
        return events.stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElse(null);
    }
    private void initializeEventChains() {
        eventSequences.put("secret_room_etap1", "secret_room_etap2");
        eventSequences.put("secret_room_etap2", "secret_room_etap3");
        eventSequences.put("radio_transmission", "radio_source");
    } // eventChain


    // === WYŚWIETLANIE STATUSU ===
    private void displayStatus() {
        System.out.println("\n[STATUS]");
        System.out.println("Zapasy jedzenia: " + gameData.getFoodSupplies());
        System.out.println("Zapasy wody: " + gameData.getWaterSupplies());
        System.out.println("Zapasy leków: " + gameData.getMedicineSupplies());

        gameData.getCharacters().forEach(c -> {
            String status = c.getName() +
                    " | Zdrowie: " + c.getHealth() +
                    " | " + (c.isHungry() ? "GŁODNY" : "Najedzony") +
                    " | " + (c.isThirsty() ? "SPRAGNIONY" : "Napojony") +
                    " | " + (c.isSick() ? "CHORY" : "Zdrowy");
            System.out.println(status);
        });

        System.out.println("\n[NASTROJE POSTACI]");
        for (Character character : gameData.getCharacters()) {
            String message = dailyStatusMessages.get(character.getName());
            String wrappedMessage = TextUtils.wrapText(character.getName() + ": " + message, 80);
            System.out.println(wrappedMessage);
        }
    }
    private void generateDailyStatusMessages() {
        dailyStatusMessages.clear();
        for (Character character : gameData.getCharacters()) {
            String message = StatusLoader.getRandomMessage(character);
            dailyStatusMessages.put(character.getName(), message);
        }
    }

    // === INTERAKCJA Z UŻYTKOWNIKIEM ===
    private boolean confirmDayEnd(Scanner scanner) {
        System.out.print("\nCzy przejść do następnego dnia? (T/N) ");
        String choice = scanner.nextLine().trim().toUpperCase();
        return choice.equals("T");
    }

    // === LOGIKA GRY ===
    private boolean isGameOver() {
        return gameData.getCharacters().stream().allMatch(c -> c.getHealth() <= 0);
    }

    // === Czyszczenie ekranu ===
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Nie udało się wyczyścić ekranu: " + e.getMessage());
        }
    }
}