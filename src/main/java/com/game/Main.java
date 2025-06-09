package main.java.com.game;

import main.java.com.game.core.GameController;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            clearScreen();
            System.out.println("=== SURVIVAL GAME ===");
            System.out.println("1. Start Gry");
            System.out.println("2. Opis Gry");
            System.out.println("3. Zakończ Grę");
            System.out.print("Wybierz opcję (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    GameController game = new GameController();
                    game.startGame(scanner);
                    break;
                case "2":
                    displayGameInfo(scanner);
                    break;
                case "3":
                    isRunning = false;
                    System.out.println("Do zobaczenia!");
                    break;
                default:
                    System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
                    waitForEnter(scanner);
            }
        }
        scanner.close();
    }

    private static void displayGameInfo(Scanner scanner) {
        clearScreen();
        System.out.println("=== OPIS GRY ===");
        System.out.println("Twoja rodzina musi przetrwać w trudnych warunkach.");
        System.out.println("Zarządzaj zasobami (jedzenie, woda, leki) i decyduj, jak reagować na wydarzenia.");
        System.out.println("Każdy dzień to nowe wyzwania. Przetrwaj jak najdłużej!");
        waitForEnter(scanner);
    }

    private static void waitForEnter(Scanner scanner) {
        System.out.print("\nNaciśnij Enter, aby kontynuować...");
        scanner.nextLine();
    }

    private static void clearScreen() {
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