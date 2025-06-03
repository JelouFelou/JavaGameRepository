package main.java.com.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameState {
    private int currentDay = 1;
    private final List<Character> characters = new ArrayList<>();
    private int foodSupplies = 3;

    public void startGame() {
        initializeCharacters();

        while(currentDay <= 5 && !isGameOver()) { // 5 day test
            System.out.println("\n=== DZIEŃ " + currentDay + " ===");
            simulateDay();
            currentDay++;
        }

        System.out.println("\nKONIEC GRY!");
    }

    private void initializeCharacters() {
        characters.add(new Character("Daniel"));
        characters.add(new Character("Monika"));
    }

    private void simulateDay() {
        // 1. Check Hunger (every 2 days)
        if(currentDay % 2 == 0) {
            System.out.println("--> Sprawdzam głód...");
            characters.forEach(c -> c.setHungry(true));
        }

        // 2. Check status
        displayStatus();

        // 3. Simulate event
        simulateRandomEvent();
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

    public void startGame(Scanner scanner) {
        initializeCharacters();

        while(currentDay <= 5 && !isGameOver()) {
            System.out.println("\n=== DZIEŃ " + currentDay + " ===");
            simulateDay(scanner);
            currentDay++;
        }

        System.out.println("\nKONIEC GRY!");
    }

    private void simulateDay(Scanner scanner) {
        // 1. Sprawdź głód (co 2 dni)
        if(currentDay % 2 == 0) {
            System.out.println("--> Sprawdzam głód...");
            characters.forEach(c -> c.setHungry(true));
        }

        // 2. Wyświetl stan
        displayStatus();

        // 3. Karmienie postaci - POPRAWIONE: przekazujemy Scanner
        handleFeeding(scanner);

        // 4. Symuluj wydarzenie
        simulateRandomEvent();

        // 5. Zastosuj efekty dnia
        characters.forEach(Character::applyDailyEffects);
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