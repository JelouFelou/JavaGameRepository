package main.java.com.game.core;

import main.java.com.game.model.Character;
import main.java.com.game.model.Event;
import main.java.com.game.model.EventOption;
import main.java.com.game.model.GameData;
import main.java.com.game.utils.EventLoader;
import main.java.com.game.utils.GameBackup;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameController {
    private final GameData gameData;
    private final GameBackup gameBackup;
    private List<Event> events;
    private final Random random = new Random();
    private int backupFoodState;

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
    }

    public void startGame(Scanner scanner) {
        initializeCharacters();

        while(gameData.getCurrentDay() <= 5 && !isGameOver()) {
            System.out.println("\n=== DZIEŃ " + gameData.getCurrentDay() + " ===");
            simulateDay(scanner);
            gameData.setCurrentDay(gameData.getCurrentDay() + 1);
        }

        System.out.println("\nKONIEC GRY!");
    }

    private void initializeCharacters() {
        gameData.getCharacters().add(new Character("Daniel"));
        gameData.getCharacters().add(new Character("Monika"));
    }

    private void simulateDay(Scanner scanner) {
        // 0. Backup stanu początkowego dnia
        List<Character> dayStartState = backupCharacters();
        int startFood = gameData.getFoodSupplies();

        boolean dayCompleted = false;
        while(!dayCompleted) {
            // 1. Sprawdź głód (co 2 dni)
            if (gameData.getCurrentDay() % 2 == 0) {
                System.out.println("--> Sprawdzam głód...");
                gameData.getCharacters().forEach(c -> c.setHungry(true));
            }

            // 2. Wyświetl status
            displayStatus();

            // 3. Wylosuj i przeprowadź wydarzenie
            Event currentEvent = getRandomEvent();
            if (currentEvent != null) {
                System.out.println("\n[WYDARZENIE]");
                System.out.println(currentEvent.getDescription());

                List<EventOption> options = currentEvent.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    System.out.printf("%d. %s%n", i + 1, options.get(i).getText());
                }

                int choice = -1;
                while (choice < 0 || choice >= options.size()) {
                    System.out.print("Twój wybór (1-" + options.size() + "): ");
                    try {
                        choice = scanner.nextInt() - 1;
                        scanner.nextLine();
                    } catch (InputMismatchException e) {
                        scanner.nextLine();
                        System.out.println("Nieprawidłowy wybór!");
                    }
                }
                applyEffects(options.get(choice).getEffects());
            }

            // 4. Karmienie postaci
            handleFeeding(scanner);

            // 5. Potwierdzenie zakończenia dnia
            dayCompleted = confirmDayEnd(scanner);

            // 6. Jeśli nie potwierdzono, przywróć stan początkowy
            if(!dayCompleted) {
                restoreCharacters(dayStartState);
                gameData.setFoodSupplies(startFood);
                System.out.println("\n--- Powrót do początku dnia ---");
            }
        }

        // 7. Efekty końca dnia
        applyDailyEffects();
    }

    private void displayStatus() {
        System.out.println("\n[STATUS]");
        System.out.println("Zapasy jedzenia: " + gameData.getFoodSupplies());

        gameData.getCharacters().forEach(c -> {
            String status = c.getName() +
                    " | Zdrowie: " + c.getHealth() +
                    " | " + (c.isHungry() ? "GŁODNY" : "Najedzony");
            System.out.println(status);
        });
    }

    private void handleFeeding(Scanner scanner) {
        System.out.println("\n[KARMIENIE]");
        for(int i = 0; i < gameData.getCharacters().size(); i++) {
            Character character = gameData.getCharacters().get(i);
            if(character.isHungry()) {
                System.out.print("Nakarmić " + character.getName() + "? (T/N) ");
                String choice = scanner.nextLine().trim().toUpperCase();

                if(choice.equals("T")) {
                    feedCharacter(i);
                }
            }
        }
    }

    private void feedCharacter(int characterIndex) {
        if(gameData.getFoodSupplies() > 0) {
            gameData.getCharacters().get(characterIndex).feed();
            gameData.setFoodSupplies(gameData.getFoodSupplies() - 1);
            System.out.println("Nakarmiono " + gameData.getCharacters().get(characterIndex).getName());
        } else {
            System.out.println("Brak jedzenia!");
        }
    }

    private boolean confirmDayEnd(Scanner scanner) {
        System.out.print("\nCzy przejść do następnego dnia? (T/N) ");
        String choice = scanner.nextLine().trim().toUpperCase();
        return choice.equals("T");
    }

    private List<Character> backupCharacters() {
        backupFoodState = gameData.getFoodSupplies();
        List<Character> backup = new ArrayList<>();
        for(Character original : gameData.getCharacters()) {
            Character copy = new Character(original.getName());
            copy.setHungry(original.isHungry());
            backup.add(copy);
        }
        return backup;
    }

    private void restoreCharacters(List<Character> backup) {
        gameData.setFoodSupplies(backupFoodState);
        for(int i = 0; i < gameData.getCharacters().size(); i++) {
            gameData.getCharacters().get(i).setHungry(backup.get(i).isHungry());
        }
    }

    private void applyDailyEffects() {
        gameData.getCharacters().forEach(Character::applyDailyEffects);
    }

    private boolean isGameOver() {
        return gameData.getCharacters().stream().allMatch(c -> c.getHealth() <= 0);
    }

    public Event getRandomEvent() {
        List<Event> availableEvents = events.stream()
                .filter(e -> random.nextDouble() <= e.getFrequency())
                .collect(Collectors.toList());

        if (availableEvents.isEmpty()) {
            return null;
        }

        return availableEvents.get(random.nextInt(availableEvents.size()));
    }

    private List<Event> createDefaultEvents() {
        return List.of(
                new Event("default", "Nic się nie wydarzyło.", List.of(
                        new EventOption("OK", Map.of())
                ), false, 1.0)
        );
    }

    private void applyEffects(Map<String, Integer> effects) {
        for (Map.Entry<String, Integer> entry : effects.entrySet()) {
            String effect = entry.getKey();
            int value = entry.getValue();

            switch (effect) {
                case "+food":
                    gameData.setFoodSupplies(gameData.getFoodSupplies() + value);
                    System.out.println("+ " + value + " jedzenia");
                    break;
                case "-food":
                    gameData.setFoodSupplies(Math.max(0, gameData.getFoodSupplies() - value));
                    System.out.println("- " + value + " jedzenia");
                    break;
                case "+health":
                    gameData.getCharacters().forEach(c -> {
                        int newHealth = Math.min(100, c.getHealth() + value);
                        c.setHealth(newHealth);
                    });
                    System.out.println("+ " + value + " zdrowia");
                    break;
                case "-health":
                    gameData.getCharacters().forEach(c -> {
                        c.setHealth(Math.max(0, c.getHealth() - value));
                    });
                    System.out.println("- " + value + " zdrowia");
                    break;
            }
        }
    }
}