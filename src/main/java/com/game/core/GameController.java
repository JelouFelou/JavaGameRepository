package main.java.com.game.core;

import main.java.com.game.model.Character;
import main.java.com.game.model.GameData;
import main.java.com.game.utils.GameBackup;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameController {
    private final GameData gameData;
    private final GameBackup gameBackup;
    private int backupFoodState;

    public GameController() {
        this.gameData = new GameData();
        this.gameBackup = new GameBackup();
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
        List<Character> dayStartState = backupCharacters();

        boolean dayCompleted = false;
        while(!dayCompleted) {
            simulateRandomEvent();

            if(gameData.getCurrentDay() % 2 == 0) {
                System.out.println("--> Sprawdzam głód...");
                gameData.getCharacters().forEach(c -> c.setHungry(true));
            }

            displayStatus();
            handleFeeding(scanner);
            dayCompleted = confirmDayEnd(scanner);

            if(!dayCompleted) {
                restoreCharacters(dayStartState);
                System.out.println("\n--- Powrót do karmienia ---");
            }
        }

        applyDailyEffects();
    }

    private void simulateRandomEvent() {
        String[] events = {
                "Nic szczególnego się nie wydarzyło.",
                "Znaleziono zapomnianą paczkę z jedzeniem! (+1 jedzenie)",
                "Ktoś zachorował po zjedzeniu podejrzanej konserwy..."
        };

        String event = events[gameData.getCurrentDay() % events.length];
        System.out.println("\n[WYDARZENIE] " + event);

        if(event.contains("+1 jedzenie")) {
            gameData.setFoodSupplies(gameData.getFoodSupplies() + 1);
        }
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
}