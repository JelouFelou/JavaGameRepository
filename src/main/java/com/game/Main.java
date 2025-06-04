package main.java.com.game;

import main.java.com.game.core.GameController;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("=== SURVIVAL GAME ===");
        Scanner scanner = new Scanner(System.in);
        GameController game = new GameController();
        game.startGame(scanner);
        scanner.close();
    }
}