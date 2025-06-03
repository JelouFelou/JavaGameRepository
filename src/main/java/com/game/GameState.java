package main.java.com.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameState {
    private int currentDay = 1;
    private final List<Character> characters = new ArrayList<>();
    private int foodSupplies = 3;
    private int backupFoodState;

    public void startGame(Scanner scanner) {
        initializeCharacters();

        while(currentDay <= 5 && !isGameOver()) {
            System.out.println("\n=== DZIEŃ " + currentDay + " ===");
            simulateDay(scanner);
            currentDay++;
        }

        System.out.println("\nKONIEC GRY!");
    }

    private void initializeCharacters() {
        characters.add(new Character("Daniel"));
        characters.add(new Character("Monika"));
    }

    private boolean confirmDayEnd(Scanner scanner) {
        System.out.print("\nCzy przejść do następnego dnia? (T/N) ");
        String choice = scanner.nextLine().trim().toUpperCase();
        return choice.equals("T");
    }

    private void simulateRandomEvent() {
        String[] events = {
                "Nic szczególnego się nie wydarzyło.",
                "Znaleziono zapomnianą paczkę z jedzeniem! (+1 jedzenie)",
                "Ktoś zachorował po zjedzeniu podejrzanej konserwy..."
        };

        String event = events[currentDay % events.length];
        System.out.println("\n[WYDARZENIE] " + event);

        // Event effect
        if(event.contains("+1 jedzenie")) foodSupplies++;
    }

    private void displayStatus() {
        System.out.println("\n[STATUS]");
        System.out.println("Zapasy jedzenia: " + foodSupplies);

        characters.forEach(c -> {
            String status = c.getName() +
                    " | Zdrowie: " + c.getHealth() +
                    " | " + (c.isHungry() ? "GŁODNY" : "Najedzony");
            System.out.println(status);
        });
    }

    private void simulateDay(Scanner scanner) {
        // 0. Reset stanu dnia (na wypadek powrotu do karmienia)
        List<Character> dayStartState = backupCharacters();

        boolean dayCompleted = false;
        while(!dayCompleted) {
            // 1. Wydarzenie dnia (teraz PRZED karmieniem)
            simulateRandomEvent();

            // 2. Sprawdź głód (co 2 dni)
            if(currentDay % 2 == 0) {
                System.out.println("--> Sprawdzam głód...");
                characters.forEach(c -> c.setHungry(true));
            }

            // 3. Wyświetl stan
            displayStatus();

            // 4. Karmienie
            handleFeeding(scanner);

            // 5. Potwierdzenie zakończenia dnia
            dayCompleted = confirmDayEnd(scanner);

            // 6. Jeśli nie potwierdzono, przywróć stan
            if(!dayCompleted) {
                restoreCharacters(dayStartState);
                System.out.println("\n--- Powrót do karmienia ---");
            }
        }

        // 7. Efekty końca dnia
        characters.forEach(Character::applyDailyEffects);
    }

    private List<Character> backupCharacters() {
        backupFoodState = foodSupplies; // Teraz poprawnie - int do int
        List<Character> backup = new ArrayList<>();
        for(Character original : characters) {
            Character copy = new Character(original.getName());
            copy.setHungry(original.isHungry());
            backup.add(copy);
        }
        return backup;
    }

    private void restoreCharacters(List<Character> backup) {
        foodSupplies = backupFoodState;
        for(int i = 0; i < characters.size(); i++) {
            characters.get(i).setHungry(backup.get(i).isHungry());
        }
    }

    public void feedCharacter(int characterIndex) {
        if(foodSupplies > 0) {
            characters.get(characterIndex).feed();
            foodSupplies--;
            System.out.println("Nakarmiono " + characters.get(characterIndex).getName());
        } else {
            System.out.println("Brak jedzenia!");
        }
    }

    private void handleFeeding(Scanner scanner) {
        System.out.println("\n[KARMIENIE]");
        for(int i = 0; i < characters.size(); i++) {
            Character character = characters.get(i);
            if(character.isHungry()) {
                System.out.print("Nakarmić " + character.getName() + "? (T/N) ");
                String choice = scanner.nextLine().trim().toUpperCase();

                if(choice.equals("T")) {
                    feedCharacter(i);  // TERAZ POPRAWNIE
                }
            }
        }
    }

    private boolean isGameOver() {
        return characters.stream().allMatch(c -> c.getHealth() <= 0);
    }
}